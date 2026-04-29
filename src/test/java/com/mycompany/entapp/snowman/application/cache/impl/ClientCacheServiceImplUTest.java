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
    private ClientCacheServiceImpl classUnderTest = new ClientCacheServiceImpl();

    @Test
    public void whenClearCache_thenInvokesPortRefreshCache() {
        classUnderTest.clearCache();

        Mockito.verify(clientCachePort, Mockito.times(1)).refreshCache();
    }
}
