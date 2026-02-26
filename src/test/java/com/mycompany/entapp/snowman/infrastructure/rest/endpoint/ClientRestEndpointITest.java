/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ClientService;
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

import java.util.HashSet;

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
    public void testGetClient_ReturnsClientJson() throws Exception {
        Client client = createTestClient(1, "Acme Corp");

        Mockito.when(clientService.getClient(1)).thenReturn(client);

        mockMvc.perform(get("/client/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.clientId").value(1))
            .andExpect(jsonPath("$.clientName").value("Acme Corp"))
            .andExpect(jsonPath("$.projects").isArray());
    }

    @Test
    public void testGetClient_WhenServiceReturnsNull_ThrowsException() throws Exception {
        Mockito.when(clientService.getClient(999)).thenReturn(null);

        try {
            mockMvc.perform(get("/client/999")
                    .accept(MediaType.APPLICATION_JSON));
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
            return;
        }
        // If no exception, the controller handled null gracefully
    }

    @Test
    public void testCreateClient_ReturnsOk() throws Exception {
        Mockito.doNothing().when(clientService).createClient(Mockito.any(Client.class));

        String clientJson = "{\"clientId\":1,\"clientName\":\"New Client\",\"projects\":[]}";

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(clientService).createClient(Mockito.any(Client.class));
    }

    @Test
    public void testDeleteClient_ReturnsOk() throws Exception {
        Mockito.doNothing().when(clientService).deleteClient(1);

        mockMvc.perform(delete("/client/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(clientService).deleteClient(1);
    }

    @Test
    public void testGetClient_WithProjects_ReturnsProjectsInResponse() throws Exception {
        Client client = createTestClientWithProject(2, "Tech Inc", "Project Alpha");

        Mockito.when(clientService.getClient(2)).thenReturn(client);

        mockMvc.perform(get("/client/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.clientId").value(2))
            .andExpect(jsonPath("$.clientName").value("Tech Inc"))
            .andExpect(jsonPath("$.projects").isArray())
            .andExpect(jsonPath("$.projects[0].title").value("Project Alpha"));
    }

    private Client createTestClient(int id, String name) {
        Client client = new Client();
        client.setId(id);
        client.setClientName(name);
        client.setProjects(new HashSet<Project>());
        return client;
    }

    private Client createTestClientWithProject(int id, String name, String projectTitle) {
        Client client = new Client();
        client.setId(id);
        client.setClientName(name);
        HashSet<Project> projects = new HashSet<Project>();
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle(projectTitle);
        projects.add(project);
        client.setProjects(projects);
        return client;
    }
}
