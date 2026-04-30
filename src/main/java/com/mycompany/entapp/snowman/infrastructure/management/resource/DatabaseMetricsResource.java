/*
 * |-------------------------------------------------
 * | Copyright © 2026 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management.resource;

import com.mycompany.entapp.snowman.infrastructure.db.monitoring.SlowQueryReporter.SlowQueryEntry;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Response payload for the {@code /db/metrics} endpoint, aggregating
 * connection-pool, query, and health information about the application's
 * database tier.
 */
public class DatabaseMetricsResource {

    private PoolStats poolStats;
    private QueryStats queryStats;
    private HealthDetails healthDetails;
    private Date generatedAt;

    public PoolStats getPoolStats() {
        return poolStats;
    }

    public void setPoolStats(PoolStats poolStats) {
        this.poolStats = poolStats;
    }

    public QueryStats getQueryStats() {
        return queryStats;
    }

    public void setQueryStats(QueryStats queryStats) {
        this.queryStats = queryStats;
    }

    public HealthDetails getHealthDetails() {
        return healthDetails;
    }

    public void setHealthDetails(HealthDetails healthDetails) {
        this.healthDetails = healthDetails;
    }

    public Date getGeneratedAt() {
        return generatedAt != null ? new Date(generatedAt.getTime()) : null;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt != null ? new Date(generatedAt.getTime()) : null;
    }

    /**
     * Connection pool statistics. Values are {@code -1} when the underlying
     * {@link javax.sql.DataSource} does not expose the corresponding metric
     * (e.g. {@code DriverManagerDataSource}).
     */
    public static class PoolStats {
        private String poolType;
        private int active = -1;
        private int idle = -1;
        private int total = -1;
        private int max = -1;
        private int min = -1;
        private boolean poolingSupported;

        public String getPoolType() {
            return poolType;
        }

        public void setPoolType(String poolType) {
            this.poolType = poolType;
        }

        public int getActive() {
            return active;
        }

        public void setActive(int active) {
            this.active = active;
        }

        public int getIdle() {
            return idle;
        }

        public void setIdle(int idle) {
            this.idle = idle;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public boolean isPoolingSupported() {
            return poolingSupported;
        }

        public void setPoolingSupported(boolean poolingSupported) {
            this.poolingSupported = poolingSupported;
        }
    }

    /**
     * Aggregate query-level statistics sourced from
     * {@link com.mycompany.entapp.snowman.infrastructure.db.monitoring.SlowQueryReporter}.
     */
    public static class QueryStats {
        private long totalObserved;
        private long slowCount;
        private long slowThresholdMs;
        private List<SlowQueryEntry> recentSlowQueries = Collections.emptyList();

        public long getTotalObserved() {
            return totalObserved;
        }

        public void setTotalObserved(long totalObserved) {
            this.totalObserved = totalObserved;
        }

        public long getSlowCount() {
            return slowCount;
        }

        public void setSlowCount(long slowCount) {
            this.slowCount = slowCount;
        }

        public long getSlowThresholdMs() {
            return slowThresholdMs;
        }

        public void setSlowThresholdMs(long slowThresholdMs) {
            this.slowThresholdMs = slowThresholdMs;
        }

        public List<SlowQueryEntry> getRecentSlowQueries() {
            return recentSlowQueries;
        }

        public void setRecentSlowQueries(List<SlowQueryEntry> recentSlowQueries) {
            this.recentSlowQueries = recentSlowQueries != null ? recentSlowQueries : Collections.<SlowQueryEntry>emptyList();
        }
    }

    /**
     * Detailed database health information including round-trip latency and
     * validation outcome.
     */
    public static class HealthDetails {
        private String status;
        private boolean validationOk;
        private long latencyMs = -1L;
        private String message;
        private Date checkedAt;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isValidationOk() {
            return validationOk;
        }

        public void setValidationOk(boolean validationOk) {
            this.validationOk = validationOk;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public void setLatencyMs(long latencyMs) {
            this.latencyMs = latencyMs;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Date getCheckedAt() {
            return checkedAt != null ? new Date(checkedAt.getTime()) : null;
        }

        public void setCheckedAt(Date checkedAt) {
            this.checkedAt = checkedAt != null ? new Date(checkedAt.getTime()) : null;
        }
    }
}
