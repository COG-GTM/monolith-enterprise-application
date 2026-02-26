/*
 * |-------------------------------------------------
 * | Copyright 2024 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import org.joda.time.DateTime;
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

import static org.mockito.Matchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEndpointTestConfig.class)
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
    public void testGetProject_ReturnsOkWithJson() throws Exception {
        Date startDate = new DateTime(2018, 1, 1, 12, 0, 0).toDate();

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(startDate);

        Mockito.when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Test Project"));
    }

    @Test(expected = Exception.class)
    public void testGetProject_ServiceReturnsNull_ThrowsNpe() throws Exception {
        Mockito.when(projectService.getProject(anyInt())).thenReturn(null);

        mockMvc.perform(get("/project/999")
                .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateProject_ReturnsOk() throws Exception {
        Mockito.doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(get("/project/create")
                .param("projectId", "1")
                .param("title", "New Project"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProject_ReturnsOk() throws Exception {
        Mockito.doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(get("/project/1/delete"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetProject_CorrectUrlMapping() throws Exception {
        Project project = new Project();
        project.setId(42);
        project.setProjectTitle("Another Project");
        Mockito.when(projectService.getProject(42)).thenReturn(project);

        mockMvc.perform(get("/project/42"))
                .andExpect(status().isOk());
    }
}
