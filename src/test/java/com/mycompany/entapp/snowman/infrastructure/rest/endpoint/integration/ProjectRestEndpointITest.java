package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ProjectRestEndpoint;
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

import java.util.Date;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ProjectRestEndpointITest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectRestEndpoint projectRestEndpoint;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectRestEndpoint).build();
    }

    @Test
    public void testGetProject_ReturnsOkWithProjectData() throws Exception {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Project Alpha");
        project.setDateStarted(new Date(1609459200000L));

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Project Alpha"));

        verify(projectService).getProject(1);
    }

    @Test
    public void testCreateProject_ReturnsOk() throws Exception {
        doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(post("/project/create")
                .param("projectId", "0")
                .param("title", "New Project"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProject_ReturnsOk() throws Exception {
        doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(get("/project/{projectId}/delete", 1))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(1);
    }

    @Test
    public void testGetProject_WithInvalidId_Returns4xx() throws Exception {
        mockMvc.perform(get("/project/{projectId}", "invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetProject_VerifiesServiceInteraction() throws Exception {
        Project project = new Project();
        project.setId(99);
        project.setProjectTitle("Verify Project");
        project.setDateStarted(new Date());

        when(projectService.getProject(99)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 99)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(projectService).getProject(99);
        Mockito.verifyNoMoreInteractions(projectService);
    }
}
