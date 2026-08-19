/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.entapp.snowman.domain.ProjectTestHelper;
import com.mycompany.entapp.snowman.domain.exception.SnowmanException;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.repository.impl.ClientRepositoryImpl;
import com.mycompany.entapp.snowman.domain.service.impl.ClientServiceImpl;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test of the whole client slice - HTTP layer down to the DAO boundary.
 * The endpoint, the resource mappers, the domain service and the repository are all the real
 * implementations; only the DAO and the outbound {@link RestTemplate} are stubbed out so that
 * neither a database nor the external client system is needed.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientRestEndpointITest {

    private static final int CLIENT_ID = 1;

    @Mock
    private ClientDao clientDao;

    @Mock
    private RestTemplate restTemplate;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        ClientRepositoryImpl clientRepository = new ClientRepositoryImpl();
        ReflectionTestUtils.setField(clientRepository, "clientDao", clientDao);

        ClientServiceImpl clientService = new ClientServiceImpl();
        ReflectionTestUtils.setField(clientService, "clientRepository", clientRepository);
        ReflectionTestUtils.setField(clientService, "restTemplate", restTemplate);

        ClientRestEndpoint clientRestEndpoint = new ClientRestEndpoint();
        ReflectionTestUtils.setField(clientRestEndpoint, "clientService", clientService);

        mockMvc = MockMvcBuilders.standaloneSetup(clientRestEndpoint).build();
    }

    @Test
    public void testGetClientInfoShouldReturnClientWithItsProjects() throws Exception {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());

        String responseBody = mockMvc.perform(get("/client/{clientId}", CLIENT_ID))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode client = new ObjectMapper().readTree(responseBody);
        assertEquals(CLIENT_ID, client.get("clientId").asInt());
        assertEquals("Client", client.get("clientName").asText());
        assertEquals("Project", client.get("projects").get(0).get("title").asText());

        // projects are already known, so the external client system is never called
        Mockito.verifyZeroInteractions(restTemplate);
    }

    @Test
    public void testCreateClientInfoShouldPersistClient() throws Exception {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(null);

        mockMvc.perform(post("/client/new")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"clientId\":1,\"clientName\":\"Client\",\"projects\":[]}"))
            .andExpect(status().isOk());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        Mockito.verify(clientDao).saveClient(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().getId());
        assertEquals("Client", captor.getValue().getClientName());
    }

    @Test
    public void testCreateClientInfoGivenClientAlreadyExists() throws Exception {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());

        try {
            mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientId\":1,\"clientName\":\"Client\",\"projects\":[]}"));
            fail("expected the endpoint to reject a client that already exists");
        } catch (Exception e) {
            // the endpoint wraps SnowmanException in a RuntimeException, MockMvc rethrows it
            assertTrue(rootCauseOf(e) instanceof SnowmanException);
        }

        Mockito.verify(clientDao, Mockito.never()).saveClient(Mockito.any(Client.class));
    }

    @Test
    public void testUpdateClientInfoShouldPersistClient() throws Exception {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());

        mockMvc.perform(post("/client/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"clientId\":1,\"clientName\":\"New Name\",\"projects\":[]}"))
            .andExpect(status().isOk());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        Mockito.verify(clientDao).saveClient(captor.capture());
        assertEquals("New Name", captor.getValue().getClientName());
    }

    @Test
    public void testDeleteClientInfoShouldRemoveClient() throws Exception {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());

        mockMvc.perform(delete("/client/{clientId}", CLIENT_ID))
            .andExpect(status().isOk());

        Mockito.verify(clientDao).removeClient(CLIENT_ID);
    }

    private Throwable rootCauseOf(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private Client client() {
        Set<Project> projects = new HashSet<>();
        projects.add(ProjectTestHelper.getProject());

        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName("Client");
        client.setProjects(projects);
        return client;
    }
}
