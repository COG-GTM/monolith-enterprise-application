/*
 * |-------------------------------------------------
 * | Copyright 2024 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.entapp.snowman.domain.exception.SnowmanException;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
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

import java.util.ArrayList;

import static org.mockito.Matchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEndpointTestConfig.class)
public class ClientRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ClientService clientService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        Mockito.reset(clientService);
    }

    @Test
    public void testGetClient_ReturnsOkWithJson() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new java.util.HashSet<com.mycompany.entapp.snowman.domain.model.Project>());

        Mockito.when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.clientName").value("Acme Corp"));
    }

    @Test(expected = Exception.class)
    public void testGetClient_ServiceReturnsNull_ThrowsNpe() throws Exception {
        Mockito.when(clientService.getClient(anyInt())).thenReturn(null);

        mockMvc.perform(get("/client/999")
                .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateClient_WithJsonBody_ReturnsOk() throws Exception {
        Mockito.doNothing().when(clientService).createClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Acme Corp");
        clientResource.setProjects(new ArrayList<com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource>());

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateClient_WithJsonBody_ReturnsOk() throws Exception {
        Mockito.doNothing().when(clientService).updateClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Updated Corp");
        clientResource.setProjects(new ArrayList<com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource>());

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteClient_ReturnsOk() throws Exception {
        Mockito.doNothing().when(clientService).deleteClient(1);

        mockMvc.perform(delete("/client/1"))
                .andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testCreateClient_ServiceThrowsException_PropagatesAsError() throws Exception {
        Mockito.doThrow(new SnowmanException("creation error"))
                .when(clientService).createClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Acme Corp");
        clientResource.setProjects(new ArrayList<com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource>());

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)));
    }

    @Test(expected = Exception.class)
    public void testUpdateClient_ServiceThrowsException_PropagatesAsError() throws Exception {
        Mockito.doThrow(new SnowmanException("update error"))
                .when(clientService).updateClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("Updated Corp");
        clientResource.setProjects(new ArrayList<com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource>());

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)));
    }

    @Test(expected = Exception.class)
    public void testDeleteClient_ServiceThrowsException_PropagatesAsError() throws Exception {
        Mockito.doThrow(new SnowmanException("delete error"))
                .when(clientService).deleteClient(1);

        mockMvc.perform(delete("/client/1"));
    }

    @Test
    public void testGetClient_CorrectUrlMapping() throws Exception {
        Client client = new Client();
        client.setId(42);
        client.setClientName("Test Client");
        client.setProjects(new java.util.HashSet<com.mycompany.entapp.snowman.domain.model.Project>());
        Mockito.when(clientService.getClient(42)).thenReturn(client);

        mockMvc.perform(get("/client/42"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetClient_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/client/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }
}
