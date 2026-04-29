/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.application.healthcheck;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HealthStatusUTest {

    @Test
    public void up_hasStatusStringUp() {
        assertEquals("UP", HealthStatus.UP.getStatusString());
    }

    @Test
    public void down_hasStatusStringDown() {
        assertEquals("DOWN", HealthStatus.DOWN.getStatusString());
    }

    @Test
    public void valueOf_returnsExpectedConstants() {
        assertEquals(HealthStatus.UP, HealthStatus.valueOf("UP"));
        assertEquals(HealthStatus.DOWN, HealthStatus.valueOf("DOWN"));
    }

    @Test
    public void values_returnsBothConstants() {
        HealthStatus[] values = HealthStatus.values();
        assertEquals(2, values.length);
    }
}
