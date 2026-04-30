/*
 * |-------------------------------------------------
 * | Copyright © 2026 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management;

import com.mycompany.entapp.snowman.infrastructure.db.health.DBHealthCheck;
import com.mycompany.entapp.snowman.infrastructure.db.monitoring.SlowQueryReporter;
import com.mycompany.entapp.snowman.infrastructure.management.resource.DatabaseMetricsResource;
import com.mycompany.entapp.snowman.infrastructure.management.resource.DatabaseMetricsResource.HealthDetails;
import com.mycompany.entapp.snowman.infrastructure.management.resource.DatabaseMetricsResource.PoolStats;
import com.mycompany.entapp.snowman.infrastructure.management.resource.DatabaseMetricsResource.QueryStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Management endpoints for the database tier:
 * <ul>
 *     <li>{@code GET  /db/metrics}     — full pool, query, and health metrics</li>
 *     <li>{@code GET  /db/health}      — detailed health (round-trip latency, validation)</li>
 *     <li>{@code POST /db/pool/reset}  — soft pool reset (best-effort, pool implementation dependent)</li>
 * </ul>
 */
@RestController
@RequestMapping("/db")
public class DatabaseManagementRestEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseManagementRestEndpoint.class);

    @Autowired
    private DBHealthCheck dbHealthCheck;

    @Autowired
    private SlowQueryReporter slowQueryReporter;

    @Autowired
    private DataSource dataSource;

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public ResponseEntity<DatabaseMetricsResource> getMetrics() {
        DatabaseMetricsResource resource = new DatabaseMetricsResource();
        resource.setGeneratedAt(new Date());
        resource.setPoolStats(buildPoolStats());
        resource.setQueryStats(buildQueryStats());
        resource.setHealthDetails(buildHealthDetails());
        return ResponseEntity.ok(resource);
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<HealthDetails> getHealth() {
        return ResponseEntity.ok(buildHealthDetails());
    }

    @RequestMapping(value = "/pool/reset", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> resetPool() {
        LOG.info("Soft pool reset requested for dataSource={}", dataSource.getClass().getName());

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("dataSource", dataSource.getClass().getName());

        boolean reset = invokeNoArg(dataSource, "evict")
            || invokeNoArg(dataSource, "softEvictConnections")
            || invokeNoArg(dataSource, "purge")
            || invokeNoArg(dataSource, "clear");

        if (reset) {
            body.put("status", "RESET");
            body.put("message", "Pool reset signal dispatched.");
        } else {
            body.put("status", "NO_OP");
            body.put("message", "Underlying DataSource does not expose a soft-reset hook; no action taken.");
        }
        return ResponseEntity.ok(body);
    }

    private QueryStats buildQueryStats() {
        QueryStats stats = new QueryStats();
        stats.setTotalObserved(slowQueryReporter.getTotalObserved());
        stats.setSlowCount(slowQueryReporter.getSlowCount());
        stats.setSlowThresholdMs(slowQueryReporter.getSlowThresholdMs());
        stats.setRecentSlowQueries(slowQueryReporter.getSlowQueries());
        return stats;
    }

    private PoolStats buildPoolStats() {
        PoolStats stats = new PoolStats();
        stats.setPoolType(dataSource.getClass().getName());

        Integer active = readIntMetric(dataSource, "getNumActive", "getActiveConnections", "getActive");
        Integer idle = readIntMetric(dataSource, "getNumIdle", "getIdleConnections", "getIdle");
        Integer total = readIntMetric(dataSource, "getTotalConnections", "getNumOpen", "getTotal");
        Integer max = readIntMetric(dataSource, "getMaxTotal", "getMaximumPoolSize", "getMaxActive", "getMaxPoolSize");
        Integer min = readIntMetric(dataSource, "getMinIdle", "getMinimumIdle", "getMinPoolSize");

        if (active != null) {
            stats.setActive(active.intValue());
        }
        if (idle != null) {
            stats.setIdle(idle.intValue());
        }
        if (total != null) {
            stats.setTotal(total.intValue());
        }
        if (max != null) {
            stats.setMax(max.intValue());
        }
        if (min != null) {
            stats.setMin(min.intValue());
        }
        stats.setPoolingSupported(active != null || idle != null || total != null || max != null);
        return stats;
    }

    private HealthDetails buildHealthDetails() {
        HealthDetails details = new HealthDetails();
        details.setCheckedAt(new Date());

        long startNanos = System.nanoTime();
        boolean ok;
        try {
            ok = dbHealthCheck.getDBStatus();
        } catch (RuntimeException e) {
            LOG.error("Database health check threw an exception", e);
            ok = false;
            details.setMessage(e.getClass().getName() + ": " + e.getMessage());
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

        details.setLatencyMs(elapsedMs);
        details.setValidationOk(ok);
        details.setStatus(ok ? "UP" : "DOWN");
        if (details.getMessage() == null) {
            details.setMessage(ok
                ? "Validation query succeeded."
                : "Validation query failed; see application logs for details.");
        }
        return details;
    }

    private static Integer readIntMetric(Object target, String... candidateMethods) {
        for (String name : candidateMethods) {
            try {
                Method m = target.getClass().getMethod(name);
                Object value = m.invoke(target);
                if (value instanceof Number) {
                    return Integer.valueOf(((Number) value).intValue());
                }
            } catch (NoSuchMethodException e) {
                // try next candidate
            } catch (Exception e) {
                LOG.debug("Failed to read pool metric {}: {}", name, e.toString());
            }
        }
        return null;
    }

    private static boolean invokeNoArg(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            m.invoke(target);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            LOG.warn("Invocation of {} on {} failed: {}", methodName, target.getClass().getName(), e.toString());
            return false;
        }
    }
}
