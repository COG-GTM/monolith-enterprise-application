/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management.resource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusResourceUTest {

    @Test
    public void getterAndSetter_storeValue() {
        StatusResource resource = new StatusResource();
        resource.setStatus("UP");

        assertEquals("UP", resource.getStatus());
    }
}
