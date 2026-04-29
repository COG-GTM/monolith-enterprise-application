/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppInfoUTest {

    @Test
    public void gettersAndSetters_storeValues() {
        AppInfo info = new AppInfo();
        info.setId(1);
        info.setVersion("2.0.1");

        assertEquals(1, info.getId());
        assertEquals("2.0.1", info.getVersion());
    }

    @Test
    public void equalsAndHashCode_followContract() {
        AppInfo a = new AppInfo();
        a.setId(1);
        a.setVersion("1.0.0");

        AppInfo b = new AppInfo();
        b.setId(1);
        b.setVersion("1.0.0");

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));

        b.setVersion("1.0.1");
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        AppInfo info = new AppInfo();
        info.setId(1);
        info.setVersion("1.0.0");
        assertNotNull(info.toString());
    }
}
