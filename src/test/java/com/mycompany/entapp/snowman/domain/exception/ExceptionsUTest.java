/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExceptionsUTest {

    @Test
    public void businessException_carriesMessage() {
        BusinessException ex = new BusinessException("boom");
        assertEquals("boom", ex.getMessage());
        assertTrue(ex instanceof Exception);
    }

    @Test
    public void snowmanException_carriesMessage() {
        SnowmanException ex = new SnowmanException("client missing");
        assertEquals("client missing", ex.getMessage());
        assertTrue(ex instanceof BusinessException);
    }
}
