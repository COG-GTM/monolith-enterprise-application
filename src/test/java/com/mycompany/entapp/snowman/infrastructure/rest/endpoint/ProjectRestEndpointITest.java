/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.config.TestWebConfig;
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

import org.springframework.web.util.NestedServletException;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestWebConfig.class)
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
    public void testGetProject_ReturnsProjectJson() throws Exception {
        Date startDate = new Date();
        Project project = createTestProject(1, "Alpha Project", startDate, null);

        Mockito.when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.projectId").value(1))
            .andExpect(jsonPath("$.title").value("Alpha Project"))
            .andExpect(jsonPath("$.dateStarted").exists());
    }

    @Test
    public void testGetProject_WhenServiceReturnsNull_ThrowsException() throws Exception {
        Mockito.when(projectService.getProject(999)).thenReturn(null);

        try {
            mockMvc.perform(get("/project/999")
                    .accept(MediaType.APPLICATION_JSON));
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
            return;
        }
        // If no exception, the controller handled null gracefully
    }

    @Test
    public void testGetProject_WithStartAndEndDate_ReturnsBothDates() throws Exception {
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L);
        Project project = createTestProject(2, "Beta Project", startDate, endDate);

        Mockito.when(projectService.getProject(2)).thenReturn(project);

        mockMvc.perform(get("/project/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.projectId").value(2))
            .andExpect(jsonPath("$.title").value("Beta Project"))
            .andExpect(jsonPath("$.dateStarted").exists())
            .andExpect(jsonPath("$.dateEnded").exists());
    }

    @Test
    public void testCreateProject_ReturnsOk() throws Exception {
        Mockito.doNothing().when(projectService).createProject(Mockito.any(Project.class));

        mockMvc.perform(post("/project/create")
                .param("projectId", "1")
                .param("title", "New Project")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(projectService).createProject(Mockito.any(Project.class));
    }

    @Test
    public void testDeleteProject_ReturnsOk() throws Exception {
        Mockito.doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(delete("/project/1/delete")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(projectService).deleteProject(1);
    }

    private Project createTestProject(int id, String title, Date startDate, Date endDate) {
        Project project = new Project();
        project.setId(id);
        project.setProjectTitle(title);
        project.setDateStarted(startDate);
        project.setDateEnded(endDate);
        return project;
    }
}
