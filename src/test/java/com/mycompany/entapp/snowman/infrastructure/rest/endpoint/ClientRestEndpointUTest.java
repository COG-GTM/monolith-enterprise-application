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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientResourceMapper.class)
public class ClientRestEndpointUTest {

    private static final int CLIENT_ID = 5;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint systemUnderTest = new ClientRestEndpoint();

    @Test
    public void testGetClientInfoShouldReturnClientResource() {
        Client client = new Client();
        ClientResource clientResource = new ClientResource();

        Mockito.when(clientService.getClient(CLIENT_ID)).thenReturn(client);
        PowerMockito.mockStatic(ClientResourceMapper.class);
        PowerMockito.when(ClientResourceMapper.mapToClientResource(client)).thenReturn(clientResource);

        ResponseEntity<ClientResource> responseEntity = systemUnderTest.getClientInfo(CLIENT_ID);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(clientResource, responseEntity.getBody());
        Mockito.verify(clientService, Mockito.times(1)).getClient(CLIENT_ID);
    }

    @Test
    public void testCreateClientInfoShouldCreateClient() throws SnowmanException {
        Client client = new Client();
        ClientResource clientResource = new ClientResource();

        PowerMockito.mockStatic(ClientResourceMapper.class);
        PowerMockito.when(ClientResourceMapper.mapToClient(clientResource)).thenReturn(client);
        Mockito.doNothing().when(clientService).createClient(client);

        systemUnderTest.createClientInfo(clientResource);

        Mockito.verify(clientService, Mockito.times(1)).createClient(client);
    }

    @Test
    public void testUpdateClientInfoShouldUpdateClient() throws SnowmanException {
        Client client = new Client();
        ClientResource clientResource = new ClientResource();

        PowerMockito.mockStatic(ClientResourceMapper.class);
        PowerMockito.when(ClientResourceMapper.mapToClient(clientResource)).thenReturn(client);
        Mockito.doNothing().when(clientService).updateClient(client);

        systemUnderTest.updateClientInfo(clientResource);

        Mockito.verify(clientService, Mockito.times(1)).updateClient(client);
    }

    @Test
    public void testDeleteClientInfoShouldDeleteClient() throws SnowmanException {
        Mockito.doNothing().when(clientService).deleteClient(CLIENT_ID);

        systemUnderTest.deleteClientInfo(CLIENT_ID);

        Mockito.verify(clientService, Mockito.times(1)).deleteClient(CLIENT_ID);
    }
}
