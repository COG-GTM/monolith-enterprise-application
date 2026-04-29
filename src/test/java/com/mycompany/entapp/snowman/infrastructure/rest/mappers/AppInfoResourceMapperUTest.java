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
import static org.junit.Assert.assertNotNull;

public class AppInfoResourceMapperUTest {

    @Test
    public void givenAppInfo_whenMapToResource_thenReturnResourceWithSameValues() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        AppInfoResource resource = AppInfoResourceMapper.mapAppInfoToResource(appInfo);

        assertNotNull(resource);
        assertEquals(1, resource.getId());
        assertEquals("1.0.0", resource.getVersion());
    }

    @Test
    public void givenAppInfoWithDefaults_whenMapToResource_thenReturnResourceWithDefaults() {
        AppInfo appInfo = new AppInfo();

        AppInfoResource resource = AppInfoResourceMapper.mapAppInfoToResource(appInfo);

        assertNotNull(resource);
        assertEquals(0, resource.getId());
        assertEquals(null, resource.getVersion());
    }
}
