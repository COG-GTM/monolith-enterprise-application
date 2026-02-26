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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
public class ProjectRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProjectService projectService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(projectService);
    }

    @Test
    public void testGetProject_shouldReturnOkWithProjectResource() throws Exception {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(new Date(1000000000000L));
        project.setDateEnded(new Date(1500000000000L));

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Test Project"))
                .andExpect(jsonPath("$.dateStarted").exists())
                .andExpect(jsonPath("$.dateEnded").exists());
    }

    @Test
    public void testCreateProject_shouldReturnOk() throws Exception {
        doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(post("/project/create")
                .param("projectId", "1")
                .param("title", "New Project"))
                .andExpect(status().isOk());

        verify(projectService).createProject(Mockito.any(Project.class));
    }

    @Test
    public void testDeleteProject_shouldReturnOk() throws Exception {
        doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(get("/project/1/delete"))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(1);
    }

    @Test
    public void testUpdateProject_shouldReturnOk() throws Exception {
        doNothing().when(projectService).updateProject(Mockito.any(Project.class));

        mockMvc.perform(get("/project/update}")
                .param("projectId", "1")
                .param("title", "Updated Project"))
                .andExpect(status().isOk());

        verify(projectService).updateProject(Mockito.any(Project.class));
    }
}
