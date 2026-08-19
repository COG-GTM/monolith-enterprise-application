/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.mappers;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.AppInfoResource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppInfoResourceMapperUTest {

    @Test
    public void testMapAppInfoToResource() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(3);
        appInfo.setVersion("1.0-SNAPSHOT");

        AppInfoResource appInfoResource = AppInfoResourceMapper.mapAppInfoToResource(appInfo);

        assertEquals(3, appInfoResource.getId());
        assertEquals("1.0-SNAPSHOT", appInfoResource.getVersion());
    }
}
