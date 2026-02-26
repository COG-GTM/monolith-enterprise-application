/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
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

public class ProjectRestEndpointITest {

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

    private Project createTestProject(int id, String title) {
        Project project = new Project();
        project.setId(id);
        project.setProjectTitle(title);
        project.setDateStarted(new Date(1000000000000L));
        project.setDateEnded(new Date(1500000000000L));
        return project;
    }

    @Test
    public void testGetProject_ReturnsOkWithJson() throws Exception {
        Project project = createTestProject(1, "Project Alpha");

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.projectId").value(1))
            .andExpect(jsonPath("$.title").value("Project Alpha"))
            .andExpect(jsonPath("$.dateStarted").exists())
            .andExpect(jsonPath("$.dateEnded").exists());

        verify(projectService).getProject(1);
    }

    @Test
    public void testGetProject_WithDifferentId() throws Exception {
        Project project = createTestProject(42, "Project Beta");

        when(projectService.getProject(42)).thenReturn(project);

        mockMvc.perform(get("/project/42"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.projectId").value(42))
            .andExpect(jsonPath("$.title").value("Project Beta"));

        verify(projectService).getProject(42);
    }

    @Test
    public void testCreateProject_ReturnsOk() throws Exception {
        doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(post("/project/create")
                .param("projectId", "1")
                .param("title", "New Project"))
            .andExpect(status().isOk());

        verify(projectService).createProject(Mockito.any(Project.class));
    }

    @Test
    public void testDeleteProject_ReturnsOk() throws Exception {
        doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(get("/project/1/delete"))
            .andExpect(status().isOk());

        verify(projectService).deleteProject(1);
    }

    @Test
    public void testGetProject_VerifiesJsonContentType() throws Exception {
        Project project = createTestProject(1, "Project Alpha");

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testGetProject_VerifiesDateSerialization() throws Exception {
        Project project = createTestProject(1, "Date Test Project");

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dateStarted").isNumber())
            .andExpect(jsonPath("$.dateEnded").isNumber());
    }

    @Test
    public void testGetProject_VerifiesCorrectRouting() throws Exception {
        Project project = createTestProject(100, "Routing Test");

        when(projectService.getProject(100)).thenReturn(project);

        mockMvc.perform(get("/project/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectId").value(100));
    }
}
