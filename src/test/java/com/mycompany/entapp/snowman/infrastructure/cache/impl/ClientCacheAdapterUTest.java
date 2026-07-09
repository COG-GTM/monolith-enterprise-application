/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.cache.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientCacheAdapterUTest {

    @InjectMocks
    private ClientCacheAdapter systemUnderTest = new ClientCacheAdapter();

    @Test
    public void testRefreshCacheShouldExecuteWithoutError() {
        // refreshCache is annotated with @CacheEvict; the method body itself
        // simply performs the (logged) eviction operation.
        systemUnderTest.refreshCache();
    }
}
