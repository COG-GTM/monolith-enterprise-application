/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("deprecation")
public class ProjectRestEndpointITest {

    private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON_UTF8;

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectRestEndpoint projectRestEndpoint;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(projectRestEndpoint).build();
    }

    @Test
    public void testGetProjectReturnsOkWithJsonBody() throws Exception {
        Date startDate = new Date(1500000000000L);
        Date endDate = new Date(1600000000000L);

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(startDate);
        project.setDateEnded(endDate);

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Test Project"))
                .andExpect(jsonPath("$.dateStarted").exists())
                .andExpect(jsonPath("$.dateEnded").exists());
    }

    @Test
    public void testGetProjectReturnsJsonContentType() throws Exception {
        Project project = new Project();
        project.setId(5);
        project.setProjectTitle("Another Project");
        project.setDateStarted(new Date());

        when(projectService.getProject(5)).thenReturn(project);

        mockMvc.perform(get("/project/5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void testCreateProjectReturnsOk() throws Exception {
        doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(post("/project/create")
                .param("projectId", "1")
                .param("title", "New Project")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(projectService).createProject(Mockito.any(Project.class));
    }

    @Test
    public void testDeleteProjectReturnsOk() throws Exception {
        doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(get("/project/1/delete"))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(1);
    }

    @Test
    public void testUpdateProjectReturnsOk() throws Exception {
        doNothing().when(projectService).updateProject(Mockito.any(Project.class));

        mockMvc.perform(get("/project/update}")
                .param("projectId", "1")
                .param("title", "Updated Project"))
                .andExpect(status().isOk());

        verify(projectService).updateProject(Mockito.any(Project.class));
    }
}
