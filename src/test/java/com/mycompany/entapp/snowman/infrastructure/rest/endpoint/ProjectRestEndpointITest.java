/*
 * Integration tests for ProjectRestEndpoint using MockMvc.
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.config.RestIntegrationTestConfig;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestIntegrationTestConfig.class)
@WebAppConfiguration
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
    public void testGetProject_returnsOkWithProjectJson() throws Exception {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(new Date(1514764800000L));

        Mockito.when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.projectId").value(1))
            .andExpect(jsonPath("$.title").value("Test Project"));
    }

    @Test
    public void testGetProject_verifyServiceCalled() throws Exception {
        Project project = new Project();
        project.setId(42);
        project.setProjectTitle("Another Project");
        project.setDateStarted(new Date());

        Mockito.when(projectService.getProject(42)).thenReturn(project);

        mockMvc.perform(get("/project/42").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(projectService).getProject(42);
    }

    @Test
    public void testCreateProject_returnsOk() throws Exception {
        mockMvc.perform(post("/project/create")
                .param("projectId", "1")
                .param("title", "New Project"))
            .andExpect(status().isOk());

        Mockito.verify(projectService).createProject(Mockito.any(Project.class));
    }

    @Test
    public void testDeleteProject_returnsOk() throws Exception {
        mockMvc.perform(get("/project/1/delete"))
            .andExpect(status().isOk());

        Mockito.verify(projectService).deleteProject(1);
    }

    @Test
    public void testGetProject_returnsJsonContentType() throws Exception {
        Project project = new Project();
        project.setId(5);
        project.setProjectTitle("ContentType Project");
        project.setDateStarted(new Date());

        Mockito.when(projectService.getProject(5)).thenReturn(project);

        mockMvc.perform(get("/project/5").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
