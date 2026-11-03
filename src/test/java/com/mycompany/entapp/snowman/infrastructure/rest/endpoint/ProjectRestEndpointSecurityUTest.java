/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.security.ApiTokenAuthInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ProjectRestEndpointSecurityUTest {

    private static final String READ_WRITE = "Bearer rw-token";
    private static final String READ_ONLY = "Bearer ro-token";

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectRestEndpoint projectRestEndpoint = new ProjectRestEndpoint();

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectRestEndpoint)
            .addMappedInterceptors(new String[]{"/project/**"}, new ApiTokenAuthInterceptor("rw-token", "ro-token"))
            .build();
        Mockito.when(projectService.getProject(Matchers.anyInt())).thenReturn(new Project());
    }

    @Test
    public void testGetDeleteIsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/project/5/delete")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/project/5/delete").header("Authorization", READ_WRITE))
            .andExpect(status().isMethodNotAllowed());

        Mockito.verify(projectService, Mockito.never()).deleteProject(Matchers.anyInt());
    }

    @Test
    public void testGetCreateAndUpdateAreMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/project/create").header("Authorization", READ_WRITE))
            .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/project/update").header("Authorization", READ_WRITE))
            .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/project/5").header("Authorization", READ_WRITE))
            .andExpect(status().isMethodNotAllowed());

        Mockito.verifyZeroInteractions(projectService);
    }

    @Test
    public void testUnauthenticatedRequestsAreUnauthorized() throws Exception {
        mockMvc.perform(get("/project/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/project/create").param("title", "x")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/project/update").param("title", "x")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/project/5/delete")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/project/5/delete").header("Authorization", "Bearer wrong"))
            .andExpect(status().isUnauthorized());

        Mockito.verifyZeroInteractions(projectService);
    }

    @Test
    public void testReadOnlyTokenCanReadButNotWrite() throws Exception {
        mockMvc.perform(get("/project/1").header("Authorization", READ_ONLY)).andExpect(status().isOk());
        mockMvc.perform(post("/project/create").param("title", "x").header("Authorization", READ_ONLY))
            .andExpect(status().isForbidden());
        mockMvc.perform(delete("/project/5/delete").header("Authorization", READ_ONLY))
            .andExpect(status().isForbidden());

        Mockito.verify(projectService).getProject(1);
        Mockito.verify(projectService, Mockito.never()).createProject(Matchers.any(Project.class));
        Mockito.verify(projectService, Mockito.never()).deleteProject(Matchers.anyInt());
    }

    @Test
    public void testReadWriteTokenReachesHandlers() throws Exception {
        mockMvc.perform(post("/project/create").param("title", "x").param("clientId", "1")
            .header("Authorization", READ_WRITE)).andExpect(status().isOk());
        mockMvc.perform(post("/project/update").param("projectId", "5").param("title", "y")
            .header("Authorization", READ_WRITE)).andExpect(status().isOk());
        mockMvc.perform(delete("/project/5/delete").header("Authorization", READ_WRITE))
            .andExpect(status().isOk());

        Mockito.verify(projectService).createProject(Matchers.any(Project.class));
        Mockito.verify(projectService).updateProject(Matchers.any(Project.class));
        Mockito.verify(projectService).deleteProject(5);
    }
}
