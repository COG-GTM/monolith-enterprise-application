/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.service.ClientService;
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

import com.mycompany.entapp.snowman.domain.model.Project;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
public class ClientRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ClientService clientService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(clientService);
    }

    @Test
    public void testGetClient_shouldReturnOkWithClientResource() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new HashSet<Project>());

        when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.clientName").value("Acme Corp"))
                .andExpect(jsonPath("$.projects").isArray());
    }

    @Test
    public void testCreateClient_shouldReturnOk() throws Exception {
        doNothing().when(clientService).createClient(Mockito.any(Client.class));

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientId\":1,\"clientName\":\"Acme Corp\",\"projects\":[]}"))
                .andExpect(status().isOk());

        verify(clientService).createClient(Mockito.any(Client.class));
    }

    @Test
    public void testUpdateClient_shouldReturnOk() throws Exception {
        doNothing().when(clientService).updateClient(Mockito.any(Client.class));

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientId\":1,\"clientName\":\"Acme Corp Updated\",\"projects\":[]}"))
                .andExpect(status().isOk());

        verify(clientService).updateClient(Mockito.any(Client.class));
    }

    @Test
    public void testDeleteClient_shouldReturnOk() throws Exception {
        doNothing().when(clientService).deleteClient(1);

        mockMvc.perform(delete("/client/1"))
                .andExpect(status().isOk());

        verify(clientService).deleteClient(1);
    }
}
