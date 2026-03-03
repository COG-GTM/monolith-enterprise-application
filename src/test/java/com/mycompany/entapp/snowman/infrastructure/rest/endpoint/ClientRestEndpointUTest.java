/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.exception.SnowmanException;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.ClientResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientResourceMapper.class)
public class ClientRestEndpointUTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint classUnderTest = new ClientRestEndpoint();

    @Test
    public void testGetClientInfo() {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        Integer clientId = 1;

        Client client = new Client();
        client.setId(clientId);
        client.setClientName("TestClient");

        ClientResource expectedClientResource = new ClientResource();
        expectedClientResource.setClientId(clientId);
        expectedClientResource.setClientName("TestClient");

        Mockito.when(clientService.getClient(clientId)).thenReturn(client);
        PowerMockito.when(ClientResourceMapper.mapToClientResource(client)).thenReturn(expectedClientResource);

        ResponseEntity<ClientResource> response = classUnderTest.getClientInfo(clientId);

        assertEquals(expectedClientResource, response.getBody());
        Mockito.verify(clientService, Mockito.times(1)).getClient(clientId);
    }

    @Test
    public void testCreateClientInfo() throws SnowmanException {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("TestClient");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        Client client = new Client();
        PowerMockito.when(ClientResourceMapper.mapToClient(clientResource)).thenReturn(client);

        Mockito.doNothing().when(clientService).createClient(client);

        classUnderTest.createClientInfo(clientResource);

        Mockito.verify(clientService, Mockito.times(1)).createClient(client);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateClientInfoThrowsException() throws SnowmanException {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("TestClient");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        Client client = new Client();
        PowerMockito.when(ClientResourceMapper.mapToClient(clientResource)).thenReturn(client);

        Mockito.doThrow(new SnowmanException("Client already exists")).when(clientService).createClient(client);

        classUnderTest.createClientInfo(clientResource);
    }

    @Test
    public void testUpdateClientInfo() throws SnowmanException {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("UpdatedClient");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        Client client = new Client();
        PowerMockito.when(ClientResourceMapper.mapToClient(clientResource)).thenReturn(client);

        Mockito.doNothing().when(clientService).updateClient(client);

        classUnderTest.updateClientInfo(clientResource);

        Mockito.verify(clientService, Mockito.times(1)).updateClient(client);
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateClientInfoThrowsException() throws SnowmanException {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("UpdatedClient");
        clientResource.setProjects(new ArrayList<ProjectResource>());

        Client client = new Client();
        PowerMockito.when(ClientResourceMapper.mapToClient(clientResource)).thenReturn(client);

        Mockito.doThrow(new SnowmanException("Client doesn't exists")).when(clientService).updateClient(client);

        classUnderTest.updateClientInfo(clientResource);
    }

    @Test
    public void testDeleteClientInfo() throws SnowmanException {
        Integer clientId = 1;

        Mockito.doNothing().when(clientService).deleteClient(clientId);

        classUnderTest.deleteClientInfo(clientId);

        Mockito.verify(clientService, Mockito.times(1)).deleteClient(clientId);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteClientInfoThrowsException() throws SnowmanException {
        Integer clientId = 1;

        Mockito.doThrow(new SnowmanException("Client doesn't exists")).when(clientService).deleteClient(clientId);

        classUnderTest.deleteClientInfo(clientId);
    }
}
