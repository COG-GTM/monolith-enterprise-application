/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClientRestEndpointITest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint clientRestEndpoint;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(clientRestEndpoint).build();
    }

    private Client createTestClient(int id, String name) {
        Client client = new Client();
        client.setId(id);
        client.setClientName(name);
        client.setProjects(new HashSet<com.mycompany.entapp.snowman.domain.model.Project>());
        return client;
    }

    private ClientResource createTestClientResource(int id, String name) {
        ClientResource resource = new ClientResource();
        resource.setClientId(id);
        resource.setClientName(name);
        resource.setProjects(new ArrayList<com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource>());
        return resource;
    }

    @Test
    public void testGetClientInfo_ReturnsOkWithJson() throws Exception {
        Client client = createTestClient(1, "Acme Corp");

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.clientId").value(1))
            .andExpect(jsonPath("$.clientName").value("Acme Corp"))
            .andExpect(jsonPath("$.projects").isArray());

        verify(clientService).getClient(1);
    }

    @Test
    public void testGetClientInfo_WithDifferentId() throws Exception {
        Client client = createTestClient(55, "Big Enterprise");

        when(clientService.getClient(55)).thenReturn(client);

        mockMvc.perform(get("/client/55"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.clientId").value(55))
            .andExpect(jsonPath("$.clientName").value("Big Enterprise"));

        verify(clientService).getClient(55);
    }

    @Test
    public void testCreateClientInfo_ReturnsOk() throws Exception {
        ClientResource resource = createTestClientResource(1, "New Client");
        doNothing().when(clientService).createClient(Mockito.any(Client.class));

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resource)))
            .andExpect(status().isOk());

        verify(clientService).createClient(Mockito.any(Client.class));
    }

    @Test
    public void testUpdateClientInfo_ReturnsOk() throws Exception {
        ClientResource resource = createTestClientResource(1, "Updated Client");
        doNothing().when(clientService).updateClient(Mockito.any(Client.class));

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resource)))
            .andExpect(status().isOk());

        verify(clientService).updateClient(Mockito.any(Client.class));
    }

    @Test
    public void testDeleteClientInfo_ReturnsOk() throws Exception {
        doNothing().when(clientService).deleteClient(1);

        mockMvc.perform(delete("/client/1"))
            .andExpect(status().isOk());

        verify(clientService).deleteClient(1);
    }

    @Test
    public void testGetClientInfo_VerifiesJsonContentType() throws Exception {
        Client client = createTestClient(1, "Acme Corp");

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testGetClientInfo_VerifiesCorrectRouting() throws Exception {
        Client client = createTestClient(10, "Routing Test Client");

        when(clientService.getClient(10)).thenReturn(client);

        mockMvc.perform(get("/client/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value(10));
    }

    @Test
    public void testUpdateClientInfo_WithJsonBody() throws Exception {
        String jsonBody = "{\"clientId\":10,\"clientName\":\"Updated JSON Client\",\"projects\":[]}";
        doNothing().when(clientService).updateClient(Mockito.any(Client.class));

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andExpect(status().isOk());

        verify(clientService).updateClient(Mockito.any(Client.class));
    }

    @Test
    public void testCreateClientInfo_WithJsonBody() throws Exception {
        String jsonBody = "{\"clientId\":5,\"clientName\":\"JSON Client\",\"projects\":[]}";
        doNothing().when(clientService).createClient(Mockito.any(Client.class));

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andExpect(status().isOk());

        verify(clientService).createClient(Mockito.any(Client.class));
    }
}
