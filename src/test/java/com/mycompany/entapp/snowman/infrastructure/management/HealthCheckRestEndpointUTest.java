/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management;

import com.mycompany.entapp.snowman.application.healthcheck.HealthCheck;
import com.mycompany.entapp.snowman.application.healthcheck.HealthStatus;
import com.mycompany.entapp.snowman.infrastructure.management.resource.StatusResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckRestEndpointUTest {

    @Mock
    private HealthCheck healthCheck;

    @InjectMocks
    private HealthCheckRestEndpoint classUnderTest = new HealthCheckRestEndpoint();

    @Test
    public void checkStatus_returnsUpStatus_whenHealthy() {
        Mockito.when(healthCheck.getHealthStatus()).thenReturn(HealthStatus.UP);

        ResponseEntity response = classUnderTest.checkStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        StatusResource resource = (StatusResource) response.getBody();
        assertEquals("UP", resource.getStatus());
    }

    @Test
    public void checkStatus_returnsDownStatus_whenUnhealthy() {
        Mockito.when(healthCheck.getHealthStatus()).thenReturn(HealthStatus.DOWN);

        ResponseEntity response = classUnderTest.checkStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StatusResource resource = (StatusResource) response.getBody();
        assertEquals("DOWN", resource.getStatus());
    }
}
