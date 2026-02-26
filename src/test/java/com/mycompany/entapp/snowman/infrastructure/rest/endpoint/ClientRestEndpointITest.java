/*
 * Integration tests for ClientRestEndpoint using MockMvc.
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.service.ClientService;
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

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestIntegrationTestConfig.class)
@WebAppConfiguration
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
    public void testGetClient_returnsOkWithClientJson() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new HashSet());

        Mockito.when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.clientId").value(1))
            .andExpect(jsonPath("$.clientName").value("Acme Corp"));
    }

    @Test
    public void testGetClient_verifyServiceCalled() throws Exception {
        Client client = new Client();
        client.setId(42);
        client.setClientName("Test Client");
        client.setProjects(new HashSet());

        Mockito.when(clientService.getClient(42)).thenReturn(client);

        mockMvc.perform(get("/client/42").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(clientService).getClient(42);
    }

    @Test
    public void testCreateClient_returnsOk() throws Exception {
        String clientJson = "{\"clientId\":1,\"clientName\":\"New Client\",\"projects\":[]}";

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
            .andExpect(status().isOk());

        Mockito.verify(clientService).createClient(Mockito.any(Client.class));
    }

    @Test
    public void testUpdateClient_returnsOk() throws Exception {
        String clientJson = "{\"clientId\":1,\"clientName\":\"Updated Client\",\"projects\":[]}";

        mockMvc.perform(post("/client/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
            .andExpect(status().isOk());

        Mockito.verify(clientService).updateClient(Mockito.any(Client.class));
    }

    @Test
    public void testDeleteClient_returnsOk() throws Exception {
        mockMvc.perform(delete("/client/1"))
            .andExpect(status().isOk());

        Mockito.verify(clientService).deleteClient(1);
    }

    @Test
    public void testGetClient_returnsJsonContentType() throws Exception {
        Client client = new Client();
        client.setId(5);
        client.setClientName("ContentType Client");
        client.setProjects(new HashSet());

        Mockito.when(clientService.getClient(5)).thenReturn(client);

        mockMvc.perform(get("/client/5").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateClient_invalidContentType_returns415() throws Exception {
        mockMvc.perform(post("/client/new")
                .contentType(MediaType.TEXT_PLAIN)
                .content("not json"))
            .andExpect(status().isUnsupportedMediaType());
    }
}
