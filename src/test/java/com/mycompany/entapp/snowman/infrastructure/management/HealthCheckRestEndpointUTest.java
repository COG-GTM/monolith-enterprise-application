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

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckRestEndpointUTest {

    @Mock
    private HealthCheck healthCheck;

    @InjectMocks
    private HealthCheckRestEndpoint systemUnderTest = new HealthCheckRestEndpoint();

    @Test
    public void testCheckStatusShouldReturnStatusOfHealthCheck() {
        Mockito.when(healthCheck.getHealthStatus()).thenReturn(HealthStatus.DOWN);

        ResponseEntity responseEntity = systemUnderTest.checkStatus();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("DOWN", ((StatusResource) responseEntity.getBody()).getStatus());
    }
}
