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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    public void testGetProject() throws Exception {
        int projectId = 1;
        Project project = createProject(projectId);

        when(projectService.getProject(projectId)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", projectId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.projectId", is(1)))
            .andExpect(jsonPath("$.title", is("Project Alpha")));

        verify(projectService, times(1)).getProject(projectId);
    }

    @Test
    public void testCreateProject() throws Exception {
        doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(post("/project/create")
                .param("projectId", "1")
                .param("title", "Project Alpha"))
            .andExpect(status().isOk());

        verify(projectService, times(1)).createProject(Mockito.any(Project.class));
    }

    @Test
    public void testDeleteProject() throws Exception {
        int projectId = 1;
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(get("/project/{projectId}/delete", projectId))
            .andExpect(status().isOk());

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    public void testUpdateProject() throws Exception {
        doNothing().when(projectService).updateProject(Mockito.any(Project.class));

        mockMvc.perform(get("/project/update}")
                .param("projectId", "1")
                .param("title", "Project Alpha Updated"))
            .andExpect(status().isOk());

        verify(projectService, times(1)).updateProject(Mockito.any(Project.class));
    }

    private Project createProject(int projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setProjectTitle("Project Alpha");
        project.setDateStarted(new Date());
        return project;
    }
}
