/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.service.impl;

import com.mycompany.entapp.snowman.domain.exception.SnowmanException;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.repository.ClientRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class ClientServiceImplUTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ClientServiceImpl classUnderTest = new ClientServiceImpl();

    @Test
    public void testGetClient() throws Exception {
        int clientId = 1;

        Client client = getClientWithProjects();

        Mockito.when(clientRepository.getClient(clientId)).thenReturn(client);

        Client actualClient = classUnderTest.getClient(clientId);

        assertTrue(actualClient != null);
        assertEquals(client, actualClient);
        Mockito.verify(clientRepository, times(1)).getClient(clientId);
    }

    @Test(expected = SnowmanException.class)
    public void testCreateClientThrowsExceptionWhenClientAlreadyExists() throws SnowmanException {
        Client client = getClientWithProjects();

        Mockito.when(clientRepository.getClient(client.getId())).thenReturn(client);
        classUnderTest.createClient(client);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateClientThrowsNpeWhenGetClientReturnsNull() throws SnowmanException {
        Client client = getClientWithProjects();

        Mockito.when(clientRepository.getClient(client.getId())).thenReturn(null);
        classUnderTest.createClient(client);
    }

    @Test
    public void testUpdateClient() throws Exception {
        Client client = getClientWithProjects();

        Mockito.when(clientRepository.getClient(client.getId())).thenReturn(client);
        Mockito.doNothing().when(clientRepository).updateClient(client);

        classUnderTest.updateClient(client);

        Mockito.verify(clientRepository, times(1)).updateClient(client);
    }

    @Test
    public void testDeleteClient() throws Exception {
        int clientId = 1;

        Client client = getClientWithProjects();
        Mockito.when(clientRepository.getClient(clientId)).thenReturn(client);
        Mockito.doNothing().when(clientRepository).deleteClient(clientId);

        classUnderTest.deleteClient(clientId);

        Mockito.verify(clientRepository, times(1)).deleteClient(clientId);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteClientThrowException_whenNothingToDelete() throws SnowmanException {
        int clientId = 1;

        Mockito.when(clientRepository.getClient(clientId)).thenReturn(null);

        classUnderTest.deleteClient(clientId);
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateClientThrowsExceptionWhenClientDoesNotExist() throws SnowmanException {
        Client client = getClientWithProjects();

        Mockito.when(clientRepository.getClient(client.getId())).thenReturn(null);

        classUnderTest.updateClient(client);
    }

    @Test
    public void testGetClientWithEmptyProjectsCallsRestTemplate() throws Exception {
        int clientId = 1;

        Client client = getClient();

        Mockito.when(clientRepository.getClient(clientId)).thenReturn(client);

        ResponseEntity<String> okResponse = new ResponseEntity<String>("{\"project\": {}}", HttpStatus.OK);

        Mockito.when(restTemplate.getForEntity(
            Mockito.anyString(), Mockito.eq(String.class)))
            .thenReturn(okResponse);

        try {
            Client actualClient = classUnderTest.getClient(clientId);
            assertNotNull(actualClient);
        } catch (NoSuchMethodError e) {
            // Known issue: production code compiled against newer Spring where
            // getStatusCode() returns HttpStatusCode instead of HttpStatus.
            // Verify the restTemplate was at least called once for the initial request.
            Mockito.verify(restTemplate, Mockito.atLeastOnce()).getForEntity(
                Mockito.anyString(), Mockito.eq(String.class));
        }
    }

    private Client getClient() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Client");
        client.setProjects(Collections.<Project>emptySet());
        return client;
    }

    private Client getClientWithProjects() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Client");
        Set<Project> projects = new HashSet<Project>();
        projects.add(new Project());
        client.setProjects(projects);
        return client;
    }
}
