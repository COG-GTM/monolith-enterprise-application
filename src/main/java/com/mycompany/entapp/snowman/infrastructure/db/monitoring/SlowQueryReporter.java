/*
 * |-------------------------------------------------
 * | Copyright © 2026 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks queries that exceed a configurable execution-time threshold and
 * exposes them for management/monitoring endpoints.
 *
 * Thread-safe: callers (DAOs, AOP interceptors, JDBC wrappers, etc.) can
 * invoke {@link #recordQuery(String, long)} concurrently from any thread.
 *
 * The component keeps a bounded in-memory ring of the most recent slow
 * queries (default capacity {@link #DEFAULT_MAX_ENTRIES}). Older entries are
 * evicted when the ring is full.
 */
@Component
public class SlowQueryReporter {

    private static final Logger LOG = LoggerFactory.getLogger(SlowQueryReporter.class);

    public static final long DEFAULT_SLOW_THRESHOLD_MS = 500L;
    public static final int DEFAULT_MAX_ENTRIES = 100;

    private final ConcurrentLinkedDeque<SlowQueryEntry> entries = new ConcurrentLinkedDeque<SlowQueryEntry>();
    private final AtomicLong totalObserved = new AtomicLong(0L);
    private final AtomicLong slowCount = new AtomicLong(0L);

    private volatile long slowThresholdMs = DEFAULT_SLOW_THRESHOLD_MS;
    private volatile int maxEntries = DEFAULT_MAX_ENTRIES;

    /**
     * Record an executed query. If {@code durationMs} meets or exceeds the
     * configured slow threshold the query is added to the bounded buffer.
     */
    public void recordQuery(String sql, long durationMs) {
        totalObserved.incrementAndGet();
        if (durationMs < slowThresholdMs) {
            return;
        }

        slowCount.incrementAndGet();

        SlowQueryEntry entry = new SlowQueryEntry(sql, durationMs, new Date());
        entries.offerLast(entry);
        trimToCapacity();

        LOG.warn("Slow query detected ({} ms, threshold {} ms): {}", durationMs, slowThresholdMs, sql);
    }

    /**
     * @return a snapshot list (most-recent first) of currently retained slow queries.
     */
    public List<SlowQueryEntry> getSlowQueries() {
        List<SlowQueryEntry> snapshot = new ArrayList<SlowQueryEntry>(entries.size());
        Iterator<SlowQueryEntry> it = entries.descendingIterator();
        while (it.hasNext()) {
            snapshot.add(it.next());
        }
        return snapshot;
    }

    public long getTotalObserved() {
        return totalObserved.get();
    }

    public long getSlowCount() {
        return slowCount.get();
    }

    public long getSlowThresholdMs() {
        return slowThresholdMs;
    }

    public void setSlowThresholdMs(long slowThresholdMs) {
        if (slowThresholdMs < 0L) {
            throw new IllegalArgumentException("slowThresholdMs must be non-negative");
        }
        this.slowThresholdMs = slowThresholdMs;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be >= 1");
        }
        this.maxEntries = maxEntries;
        trimToCapacity();
    }

    /**
     * Clear retained slow queries and reset counters. Threshold/capacity are preserved.
     */
    public void clear() {
        entries.clear();
        totalObserved.set(0L);
        slowCount.set(0L);
    }

    private void trimToCapacity() {
        int cap = maxEntries;
        while (entries.size() > cap) {
            if (entries.pollFirst() == null) {
                break;
            }
        }
    }

    /**
     * Immutable record of a single slow query observation.
     */
    public static final class SlowQueryEntry {
        private final String sql;
        private final long durationMs;
        private final Date observedAt;

        public SlowQueryEntry(String sql, long durationMs, Date observedAt) {
            this.sql = sql;
            this.durationMs = durationMs;
            this.observedAt = observedAt != null ? new Date(observedAt.getTime()) : null;
        }

        public String getSql() {
            return sql;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public Date getObservedAt() {
            return observedAt != null ? new Date(observedAt.getTime()) : null;
        }
    }
}
