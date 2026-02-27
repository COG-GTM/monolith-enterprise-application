package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.contract;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ClientRestEndpoint;
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
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Client REST endpoint.
 * Validates that the JSON response structure conforms to the expected API contract.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientRestEndpointContractTest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint clientRestEndpoint;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientRestEndpoint).build();
    }

    @Test
    public void testGetClient_ResponseContainsRequiredFields() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.clientId").exists())
                .andExpect(jsonPath("$.clientName").exists())
                .andExpect(jsonPath("$.projects").exists());
    }

    @Test
    public void testGetClient_ResponseFieldTypes() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").isNumber())
                .andExpect(jsonPath("$.clientName").isString())
                .andExpect(jsonPath("$.projects").isArray());
    }

    @Test
    public void testGetClient_ResponseHasNoUnexpectedFields() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Test");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(1)).thenReturn(client);

        MvcResult result = mockMvc.perform(get("/client/{clientId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertTrue("Response should contain clientId", json.contains("\"clientId\""));
        assertTrue("Response should contain clientName", json.contains("\"clientName\""));
        assertTrue("Response should contain projects", json.contains("\"projects\""));
        assertFalse("Response should not contain internal id field", json.contains("\"id\""));
    }

    @Test
    public void testGetClient_WithProjects_ProjectStructureIsCorrect() throws Exception {
        Project project = new Project();
        project.setId(10);
        project.setProjectTitle("Alpha");
        project.setDateStarted(new Date(1609459200000L));

        Set<Project> projects = new HashSet<Project>();
        projects.add(project);

        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme");
        client.setProjects(projects);

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects[0].projectId").exists())
                .andExpect(jsonPath("$.projects[0].title").exists())
                .andExpect(jsonPath("$.projects[0].dateStarted").exists());
    }

    @Test
    public void testGetClient_ContentTypeIsJson() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Test");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }
}
