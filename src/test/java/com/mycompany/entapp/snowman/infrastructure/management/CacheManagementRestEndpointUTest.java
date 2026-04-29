/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management;

import com.mycompany.entapp.snowman.application.cache.ClientCacheService;
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
public class CacheManagementRestEndpointUTest {

    @Mock
    private ClientCacheService clientCacheService;

    @InjectMocks
    private CacheManagementRestEndpoint classUnderTest = new CacheManagementRestEndpoint();

    @Test
    public void clearClientCache_invokesServiceAndReturnsOk() {
        ResponseEntity response = classUnderTest.clearClientCache("clientFindCache");

        Mockito.verify(clientCacheService, Mockito.times(1)).clearCache();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
