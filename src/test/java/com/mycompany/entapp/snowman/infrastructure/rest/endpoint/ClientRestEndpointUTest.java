/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.exception.SnowmanException;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ClientRestEndpointUTest {

    private static final int CLIENT_ID = 1;
    private static final String CLIENT_NAME = "Client";

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint systemUnderTest = new ClientRestEndpoint();

    @Test
    public void testGetClientInfoShouldReturnMappedClientResource() {
        Mockito.when(clientService.getClient(CLIENT_ID)).thenReturn(client());

        ResponseEntity<ClientResource> responseEntity = systemUnderTest.getClientInfo(CLIENT_ID);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(CLIENT_ID, responseEntity.getBody().getClientId());
        assertEquals(CLIENT_NAME, responseEntity.getBody().getClientName());
    }

    @Test
    public void testCreateClientInfoShouldCreateMappedClient() throws SnowmanException {
        systemUnderTest.createClientInfo(clientResource());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        Mockito.verify(clientService).createClient(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().getId());
        assertEquals(CLIENT_NAME, captor.getValue().getClientName());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateClientInfoShouldWrapSnowmanException() throws SnowmanException {
        Mockito.doThrow(new SnowmanException("Client already exists"))
            .when(clientService).createClient(Mockito.any(Client.class));

        systemUnderTest.createClientInfo(clientResource());
    }

    @Test
    public void testUpdateClientInfoShouldUpdateMappedClient() throws SnowmanException {
        systemUnderTest.updateClientInfo(clientResource());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        Mockito.verify(clientService).updateClient(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().getId());
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateClientInfoShouldWrapSnowmanException() throws SnowmanException {
        Mockito.doThrow(new SnowmanException("Client doesn't exists"))
            .when(clientService).updateClient(Mockito.any(Client.class));

        systemUnderTest.updateClientInfo(clientResource());
    }

    @Test
    public void testDeleteClientInfoShouldDeleteClient() throws SnowmanException {
        systemUnderTest.deleteClientInfo(CLIENT_ID);

        Mockito.verify(clientService).deleteClient(CLIENT_ID);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteClientInfoShouldWrapSnowmanException() throws SnowmanException {
        Mockito.doThrow(new SnowmanException("Client doesn't exists")).when(clientService).deleteClient(CLIENT_ID);

        systemUnderTest.deleteClientInfo(CLIENT_ID);
    }

    private Client client() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName(CLIENT_NAME);
        client.setProjects(Collections.<Project>emptySet());
        return client;
    }

    private ClientResource clientResource() {
        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(CLIENT_ID);
        clientResource.setClientName(CLIENT_NAME);
        clientResource.setProjects(Collections.<ProjectResource>emptyList());
        return clientResource;
    }
}
