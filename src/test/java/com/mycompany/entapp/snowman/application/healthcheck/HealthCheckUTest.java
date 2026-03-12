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
    private HealthCheck classUnderTest;

    @Test
    public void testGetHealthStatusReturnsUpWhenDBStatusIsTrue() {
        Mockito.when(dbHealthCheck.getDBStatus()).thenReturn(true);

        HealthStatus result = classUnderTest.getHealthStatus();

        assertEquals(HealthStatus.UP, result);
    }

    @Test
    public void testGetHealthStatusReturnsDownWhenDBStatusIsFalse() {
        Mockito.when(dbHealthCheck.getDBStatus()).thenReturn(false);

        HealthStatus result = classUnderTest.getHealthStatus();

        assertEquals(HealthStatus.DOWN, result);
    }
}
