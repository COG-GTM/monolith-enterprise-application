package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ClientRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
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

import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ClientRestEndpointITest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint clientRestEndpoint;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientRestEndpoint).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetClientInfo_ReturnsOkWithClientData() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.clientName").value("Acme Corp"));

        verify(clientService).getClient(1);
    }

    @Test
    public void testCreateClientInfo_ReturnsOk() throws Exception {
        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("New Client");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateClientInfo_ReturnsOk() throws Exception {
        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Updated Client");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteClientInfo_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/client/{clientId}", 1))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetClientInfo_WithInvalidId_Returns4xx() throws Exception {
        mockMvc.perform(get("/client/{clientId}", "invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetClientInfo_VerifiesServiceInteraction() throws Exception {
        Client client = new Client();
        client.setId(42);
        client.setClientName("Test Client");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(42)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", 42)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(clientService).getClient(42);
        Mockito.verifyNoMoreInteractions(clientService);
    }
}
