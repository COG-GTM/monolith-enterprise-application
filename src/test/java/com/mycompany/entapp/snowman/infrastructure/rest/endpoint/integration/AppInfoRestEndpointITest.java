package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.exception.BusinessException;
import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.AppInfoRestEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class AppInfoRestEndpointITest {

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
    public void testGetApplicationInformation_ReturnsOkWithAppInfo() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.version").value("1.0.0"));

        verify(applicationInfoService).getAppInfo();
    }

    @Test(expected = NestedServletException.class)
    public void testGetApplicationInformation_WhenServiceThrowsException_PropagatesAsError() throws Exception {
        when(applicationInfoService.getAppInfo()).thenThrow(new BusinessException("Service unavailable"));

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetApplicationInformation_VerifiesServiceInteraction() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("2.0.0");

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(applicationInfoService).getAppInfo();
        Mockito.verifyNoMoreInteractions(applicationInfoService);
    }

    @Test
    public void testGetApplicationInformation_ReturnsJsonContentType() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }
}
