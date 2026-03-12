/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.application.cache.impl;

import com.mycompany.entapp.snowman.infrastructure.cache.ClientCachePort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientCacheServiceImplUTest {

    @Mock
    private ClientCachePort clientCachePort;

    @InjectMocks
    private ClientCacheServiceImpl classUnderTest;

    @Test
    public void testClearCache() {
        Mockito.doNothing().when(clientCachePort).refreshCache();

        classUnderTest.clearCache();

        Mockito.verify(clientCachePort, Mockito.times(1)).refreshCache();
    }
}
