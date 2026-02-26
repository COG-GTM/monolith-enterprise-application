/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ClientService;
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
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ClientRestEndpointITest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint clientRestEndpoint;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientRestEndpoint).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetClientInfo() throws Exception {
        int clientId = 1;
        Client client = createClient(clientId);

        when(clientService.getClient(clientId)).thenReturn(client);

        mockMvc.perform(get("/client/{clientId}", clientId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.clientId", is(1)))
            .andExpect(jsonPath("$.clientName", is("Acme Corp")));

        verify(clientService, times(1)).getClient(clientId);
    }

    @Test
    public void testCreateClientInfo() throws Exception {
        doNothing().when(clientService).createClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Acme Corp");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
            .andExpect(status().isOk());

        verify(clientService, times(1)).createClient(Mockito.any(Client.class));
    }

    @Test
    public void testUpdateClientInfo() throws Exception {
        doNothing().when(clientService).updateClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Acme Corp Updated");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
            .andExpect(status().isOk());

        verify(clientService, times(1)).updateClient(Mockito.any(Client.class));
    }

    @Test
    public void testDeleteClientInfo() throws Exception {
        int clientId = 1;
        doNothing().when(clientService).deleteClient(clientId);

        mockMvc.perform(delete("/client/{clientId}", clientId))
            .andExpect(status().isOk());

        verify(clientService, times(1)).deleteClient(clientId);
    }

    private Client createClient(int clientId) {
        Client client = new Client();
        client.setId(clientId);
        client.setClientName("Acme Corp");
        Set<Project> projects = new HashSet<Project>();
        client.setProjects(projects);
        return client;
    }
}
