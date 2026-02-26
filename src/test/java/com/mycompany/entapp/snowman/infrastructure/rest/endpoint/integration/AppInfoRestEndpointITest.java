/*
 * |-------------------------------------------------
 * | Copyright 2024 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.exception.BusinessException;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEndpointTestConfig.class)
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
    public void testGetAppInfo_ReturnsOkWithJson() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        Mockito.when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    public void testGetAppInfo_CorrectHttpMethod() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("2.0.0");

        Mockito.when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testGetAppInfo_ServiceThrowsException_PropagatesAsError() throws Exception {
        Mockito.when(applicationInfoService.getAppInfo()).thenThrow(new BusinessException("error"));

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetAppInfo_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testGetAppInfo_DeleteMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testGetAppInfo_ResponseFieldValues() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(42);
        appInfo.setVersion("3.2.1");

        Mockito.when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.version").value("3.2.1"));
    }
}
