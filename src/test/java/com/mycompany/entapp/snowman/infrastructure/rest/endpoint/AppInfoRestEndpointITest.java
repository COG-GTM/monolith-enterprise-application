/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
public class AppInfoRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ApplicationInfoService applicationInfoService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(applicationInfoService);
    }

    @Test
    public void testGetAppInfo_shouldReturnOkWithAppInfoResource() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
