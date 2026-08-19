/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.application.healthcheck;

import com.mycompany.entapp.snowman.infrastructure.db.health.DBHealthCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckUTest {

    @Mock
    private DBHealthCheck dbHealthCheck;

    @InjectMocks
    private HealthCheck systemUnderTest = new HealthCheck();

    @Test
    public void testGetHealthStatusShouldBeUpWhenDatabaseIsReachable() {
        Mockito.when(dbHealthCheck.getDBStatus()).thenReturn(true);

        assertEquals(HealthStatus.UP, systemUnderTest.getHealthStatus());
    }

    @Test
    public void testGetHealthStatusShouldBeDownWhenDatabaseIsUnreachable() {
        Mockito.when(dbHealthCheck.getDBStatus()).thenReturn(false);

        assertEquals(HealthStatus.DOWN, systemUnderTest.getHealthStatus());
    }
}
