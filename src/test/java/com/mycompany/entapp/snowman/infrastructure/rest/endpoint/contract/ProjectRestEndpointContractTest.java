package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.contract;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ProjectRestEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Project REST endpoint.
 * Validates that the JSON response structure conforms to the expected API contract.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectRestEndpointContractTest {

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
    public void testGetProject_ResponseContainsRequiredFields() throws Exception {
        Project project = createTestProject();

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.projectId").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.dateStarted").exists());
    }

    @Test
    public void testGetProject_ResponseFieldTypes() throws Exception {
        Project project = createTestProject();

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").isNumber())
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.dateStarted").isNumber());
    }

    @Test
    public void testGetProject_ResponseHasNoUnexpectedInternalFields() throws Exception {
        Project project = createTestProject();

        when(projectService.getProject(1)).thenReturn(project);

        MvcResult result = mockMvc.perform(get("/project/{projectId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertTrue("Response should contain projectId", json.contains("\"projectId\""));
        assertTrue("Response should contain title", json.contains("\"title\""));
        assertTrue("Response should contain dateStarted", json.contains("\"dateStarted\""));
        assertFalse("Response should not expose internal projectTitle field", json.contains("\"projectTitle\""));
        assertFalse("Response should not expose client relationship", json.contains("\"client\""));
        assertFalse("Response should not expose employeeProjects", json.contains("\"employeeProjects\""));
    }

    @Test
    public void testGetProject_ContentTypeIsJson() throws Exception {
        Project project = createTestProject();

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void testGetProject_FieldValuesMappedCorrectly() throws Exception {
        Project project = createTestProject();

        when(projectService.getProject(1)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Project Alpha"));
    }

    @Test
    public void testGetProject_WithNullDateEnded_FieldIsNull() throws Exception {
        Project project = new Project();
        project.setId(2);
        project.setProjectTitle("Open Project");
        project.setDateStarted(new Date(1609459200000L));
        project.setDateEnded(null);

        when(projectService.getProject(2)).thenReturn(project);

        mockMvc.perform(get("/project/{projectId}", 2)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateEnded").doesNotExist());
    }

    private Project createTestProject() {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Project Alpha");
        project.setDateStarted(new Date(1609459200000L));
        project.setDateEnded(new Date(1640995200000L));
        return project;
    }
}
