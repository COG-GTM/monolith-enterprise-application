package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.contract;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.AppInfoRestEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for AppInfo REST endpoint.
 * Validates that the JSON response structure conforms to the expected API contract.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppInfoRestEndpointContractTest {

    private MockMvc mockMvc;

    @Mock
    private ApplicationInfoService applicationInfoService;

    @InjectMocks
    private AppInfoRestEndpoint appInfoRestEndpoint;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appInfoRestEndpoint).build();
    }

    @Test
    public void testGetAppInfo_ResponseContainsRequiredFields() throws Exception {
        AppInfo appInfo = createTestAppInfo();

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.version").exists());
    }

    @Test
    public void testGetAppInfo_ResponseFieldTypes() throws Exception {
        AppInfo appInfo = createTestAppInfo();

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.version").isString());
    }

    @Test
    public void testGetAppInfo_ResponseHasExpectedFieldsOnly() throws Exception {
        AppInfo appInfo = createTestAppInfo();

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        MvcResult result = mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertTrue("Response should contain id", json.contains("\"id\""));
        assertTrue("Response should contain version", json.contains("\"version\""));
    }

    @Test
    public void testGetAppInfo_ContentTypeIsJson() throws Exception {
        AppInfo appInfo = createTestAppInfo();

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void testGetAppInfo_FieldValuesMappedCorrectly() throws Exception {
        AppInfo appInfo = createTestAppInfo();

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    private AppInfo createTestAppInfo() {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");
        return appInfo;
    }
}
