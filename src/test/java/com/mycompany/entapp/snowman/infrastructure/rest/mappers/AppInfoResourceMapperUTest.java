/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.mappers;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.AppInfoResource;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppInfoResourceMapperUTest {

    @Test
    public void testMapAppInfoToResource() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        AppInfoResource result = AppInfoResourceMapper.mapAppInfoToResource(appInfo);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("1.0.0", result.getVersion());
    }

    @Test
    public void testMapAppInfoToResourceWithNullVersion() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(2);
        appInfo.setVersion(null);

        AppInfoResource result = AppInfoResourceMapper.mapAppInfoToResource(appInfo);

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertNull(result.getVersion());
    }
}
