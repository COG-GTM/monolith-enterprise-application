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
    public void givenAppInfo_whenMapAppInfoToResource_thenReturnAppInfoResource() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        AppInfoResource actualResource = AppInfoResourceMapper.mapAppInfoToResource(appInfo);

        assertEquals(1, actualResource.getId());
        assertEquals("1.0.0", actualResource.getVersion());
    }
}
