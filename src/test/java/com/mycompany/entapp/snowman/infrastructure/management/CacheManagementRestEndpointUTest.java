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

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheManagementRestEndpointUTest {

    @Mock
    private ClientCacheService clientCacheService;

    @InjectMocks
    private CacheManagementRestEndpoint systemUnderTest = new CacheManagementRestEndpoint();

    @Test
    public void testClearClientCacheShouldClearCacheAndReturnOk() {
        Mockito.doNothing().when(clientCacheService).clearCache();

        ResponseEntity<?> responseEntity = systemUnderTest.clearClientCache("clientFindCache");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        Mockito.verify(clientCacheService, Mockito.times(1)).clearCache();
    }
}
