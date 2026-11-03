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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CacheManagementRestEndpointUTest {

    @Mock
    private ClientCacheService clientCacheService;

    @InjectMocks
    private CacheManagementRestEndpoint classUnderTest = new CacheManagementRestEndpoint();

    @Test
    public void testClearClientCache_knownCache_clearsCache() {
        assertEquals(HttpStatus.OK, classUnderTest.clearClientCache("clientFindCache").getStatusCode());
        Mockito.verify(clientCacheService).clearCache();
    }

    @Test
    public void testClearClientCache_unknownCache_returns404WithoutClearing() {
        assertEquals(HttpStatus.NOT_FOUND, classUnderTest.clearClientCache("anything").getStatusCode());
        Mockito.verifyZeroInteractions(clientCacheService);
    }

    @Test
    public void testClearClientCache_onlyMappedToPost() throws NoSuchMethodException {
        Method method = CacheManagementRestEndpoint.class.getMethod("clearClientCache", String.class);
        assertArrayEquals(new RequestMethod[]{RequestMethod.POST}, method.getAnnotation(RequestMapping.class).method());
    }
}
