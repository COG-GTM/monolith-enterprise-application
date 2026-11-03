/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.health;

import com.mycompany.entapp.snowman.infrastructure.db.dao.AbstractJDBCDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Database liveness probe backing the anonymous /health endpoint. The result is cached for
 * {@code health.db.cacheTtlMillis} and at most one probe runs at a time, so request volume on
 * /health does not translate into database connections.
 */
@Component
public class DBHealthCheck extends AbstractJDBCDao {

    private static final Logger LOG = LoggerFactory.getLogger(DBHealthCheck.class);

    private static final String SELECT_MIN_1_FROM_APP_INFO = "SELECT min(1) from app_info";

    private final ReentrantLock probeLock = new ReentrantLock();

    private volatile CachedStatus cachedStatus;

    @Value("${health.db.cacheTtlMillis:5000}")
    private long cacheTtlMillis = 5000;

    @Value("${health.db.queryTimeoutSeconds:2}")
    private int queryTimeoutSeconds = 2;

    public boolean getDBStatus() {
        CachedStatus current = cachedStatus;
        if (isFresh(current)) {
            return current.up;
        }

        if (!probeLock.tryLock()) {
            if (current != null) {
                return current.up;
            }
            probeLock.lock();
        }

        try {
            current = cachedStatus;
            if (isFresh(current)) {
                return current.up;
            }
            boolean up = probeDatabase();
            cachedStatus = new CachedStatus(up, nanoTime());
            return up;
        } finally {
            probeLock.unlock();
        }
    }

    public void setCacheTtlMillis(long cacheTtlMillis) {
        this.cacheTtlMillis = cacheTtlMillis;
    }

    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    protected long nanoTime() {
        return System.nanoTime();
    }

    private boolean isFresh(CachedStatus status) {
        return status != null && nanoTime() - status.checkedAtNanos < TimeUnit.MILLISECONDS.toNanos(cacheTtlMillis);
    }

    private boolean probeDatabase() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.setQueryTimeout(queryTimeoutSeconds);
            try (ResultSet rs = stmt.executeQuery(SELECT_MIN_1_FROM_APP_INFO)) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.error("Database health check failed", e);
            return false;
        }
    }

    private static final class CachedStatus {
        private final boolean up;
        private final long checkedAtNanos;

        private CachedStatus(boolean up, long checkedAtNanos) {
            this.up = up;
            this.checkedAtNanos = checkedAtNanos;
        }
    }
}
