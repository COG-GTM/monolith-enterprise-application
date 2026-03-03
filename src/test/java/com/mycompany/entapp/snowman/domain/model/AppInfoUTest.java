/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppInfoUTest {

    @Test
    public void testGettersAndSetters() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("2.0.0");

        assertEquals(1, appInfo.getId());
        assertEquals("2.0.0", appInfo.getVersion());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0");

        assertTrue(appInfo.equals(appInfo));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        AppInfo info1 = new AppInfo();
        info1.setId(1);
        info1.setVersion("1.0");

        AppInfo info2 = new AppInfo();
        info2.setId(1);
        info2.setVersion("1.0");

        assertTrue(info1.equals(info2));
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentId() {
        AppInfo info1 = new AppInfo();
        info1.setId(1);
        info1.setVersion("1.0");

        AppInfo info2 = new AppInfo();
        info2.setId(2);
        info2.setVersion("1.0");

        assertFalse(info1.equals(info2));
    }

    @Test
    public void testEqualsAndHashCode_DifferentVersion() {
        AppInfo info1 = new AppInfo();
        info1.setId(1);
        info1.setVersion("1.0");

        AppInfo info2 = new AppInfo();
        info2.setId(1);
        info2.setVersion("2.0");

        assertFalse(info1.equals(info2));
    }

    @Test
    public void testEquals_Null() {
        AppInfo appInfo = new AppInfo();
        assertFalse(appInfo.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        AppInfo appInfo = new AppInfo();
        assertFalse(appInfo.equals("not an AppInfo"));
    }

    @Test
    public void testToString() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        String result = appInfo.toString();
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("version=1.0.0"));
    }
}
