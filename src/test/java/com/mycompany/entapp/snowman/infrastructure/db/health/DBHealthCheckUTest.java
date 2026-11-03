/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.health;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DBHealthCheckUTest {

    private DataSource dataSource;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private FakeClockDBHealthCheck classUnderTest;

    @Before
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        classUnderTest = new FakeClockDBHealthCheck();
        classUnderTest.setDataSource(dataSource);
        classUnderTest.setCacheTtlMillis(5000);
    }

    @Test
    public void testGetDBStatus_shouldReturnTrueAndReleaseConnection_whenQueryReturnsRow() throws SQLException {
        assertTrue(classUnderTest.getDBStatus());

        verify(statement).setQueryTimeout(2);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    public void testGetDBStatus_shouldReturnFalse_whenConnectionCannotBeObtained() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("pool exhausted"));

        assertFalse(classUnderTest.getDBStatus());
    }

    @Test
    public void testGetDBStatus_shouldReturnFalseAndReleaseConnection_whenQueryFails() throws SQLException {
        when(statement.executeQuery(anyString())).thenThrow(new SQLException("boom"));

        assertFalse(classUnderTest.getDBStatus());
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    public void testGetDBStatus_shouldReuseCachedResultWithinTtl() throws SQLException {
        for (int i = 0; i < 1000; i++) {
            assertTrue(classUnderTest.getDBStatus());
            classUnderTest.advanceMillis(4);
        }

        verify(dataSource, times(1)).getConnection();
    }

    @Test
    public void testGetDBStatus_shouldProbeAgainAfterTtlExpires() throws SQLException {
        assertTrue(classUnderTest.getDBStatus());

        when(resultSet.next()).thenReturn(false);
        classUnderTest.advanceMillis(5000);

        assertFalse(classUnderTest.getDBStatus());
        verify(dataSource, times(2)).getConnection();
    }

    @Test
    public void testGetDBStatus_shouldCacheDownResultToo() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        for (int i = 0; i < 100; i++) {
            assertFalse(classUnderTest.getDBStatus());
        }

        verify(dataSource, times(1)).getConnection();
    }

    @Test
    public void testGetDBStatus_shouldRunOneProbe_whenManyRequestsArriveConcurrently() throws Exception {
        final CountDownLatch probeStarted = new CountDownLatch(1);
        final CountDownLatch releaseProbe = new CountDownLatch(1);
        final AtomicInteger connectionsRequested = new AtomicInteger();
        DataSource slowDataSource = mock(DataSource.class);
        when(slowDataSource.getConnection()).thenAnswer(new org.mockito.stubbing.Answer<Connection>() {
            @Override
            public Connection answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                connectionsRequested.incrementAndGet();
                probeStarted.countDown();
                releaseProbe.await(5, TimeUnit.SECONDS);
                return connection;
            }
        });
        classUnderTest.setDataSource(slowDataSource);

        int requests = 200;
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                results.add(pool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return classUnderTest.getDBStatus();
                    }
                }));
            }
            assertTrue(probeStarted.await(5, TimeUnit.SECONDS));
            releaseProbe.countDown();
            for (Future<Boolean> result : results) {
                assertTrue(result.get(5, TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, connectionsRequested.get());
    }

    @Test
    public void testGetDBStatus_shouldStayWithinPoolMaximum_whenTtlDisabledUnderConcurrentLoad() throws Exception {
        ComboPooledDataSource pooled = new ComboPooledDataSource();
        pooled.setDriverClass("org.h2.Driver");
        pooled.setJdbcUrl("jdbc:h2:mem:healthcheck;DB_CLOSE_DELAY=-1");
        pooled.setUser("sa");
        pooled.setPassword("");
        pooled.setMinPoolSize(1);
        pooled.setMaxPoolSize(3);
        pooled.setCheckoutTimeout(5000);
        try {
            try (Connection setup = pooled.getConnection(); Statement stmt = setup.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS app_info (id INT PRIMARY KEY, version VARCHAR(20))");
                stmt.execute("MERGE INTO app_info KEY (id) VALUES (1, '1.0.0')");
            }

            final DBHealthCheck healthCheck = new DBHealthCheck();
            healthCheck.setDataSource(pooled);
            healthCheck.setCacheTtlMillis(0);

            ExecutorService pool = Executors.newFixedThreadPool(50);
            try {
                List<Future<Boolean>> results = new ArrayList<>();
                for (int i = 0; i < 500; i++) {
                    results.add(pool.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                            return healthCheck.getDBStatus();
                        }
                    }));
                }
                for (Future<Boolean> result : results) {
                    assertTrue(result.get(30, TimeUnit.SECONDS));
                }
            } finally {
                pool.shutdownNow();
            }

            assertTrue("physical connections: " + pooled.getNumConnectionsAllUsers(),
                pooled.getNumConnectionsAllUsers() <= 3);
        } finally {
            pooled.close();
        }
    }

    private static class FakeClockDBHealthCheck extends DBHealthCheck {
        private long nanos = 1000L;

        void advanceMillis(long millis) {
            nanos += TimeUnit.MILLISECONDS.toNanos(millis);
        }

        @Override
        protected long nanoTime() {
            return nanos;
        }
    }
}
