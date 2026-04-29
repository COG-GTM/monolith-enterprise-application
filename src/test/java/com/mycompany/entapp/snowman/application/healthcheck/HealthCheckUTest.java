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
    private HealthCheck classUnderTest = new HealthCheck();

    @Test
    public void givenDBHealthy_whenGetHealthStatus_thenReturnUp() {
        Mockito.when(dbHealthCheck.getDBStatus()).thenReturn(true);

        assertEquals(HealthStatus.UP, classUnderTest.getHealthStatus());
    }

    @Test
    public void givenDBUnhealthy_whenGetHealthStatus_thenReturnDown() {
        Mockito.when(dbHealthCheck.getDBStatus()).thenReturn(false);

        assertEquals(HealthStatus.DOWN, classUnderTest.getHealthStatus());
    }
}
