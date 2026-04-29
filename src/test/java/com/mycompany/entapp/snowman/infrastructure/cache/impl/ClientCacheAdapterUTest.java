/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.cache.impl;

import org.junit.Test;

public class ClientCacheAdapterUTest {

    private final ClientCacheAdapter classUnderTest = new ClientCacheAdapter();

    @Test
    public void refreshCache_doesNotThrow() {
        classUnderTest.refreshCache();
    }
}
