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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientResourceMapper.class)
public class ClientRestEndpointUTest {

    private static final int CLIENT_ID = 1;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientRestEndpoint classUnderTest = new ClientRestEndpoint();

    @Test
    public void testGetClientInfo() {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        Client client = new Client();
        client.setId(CLIENT_ID);

        ClientResource expectedResource = new ClientResource();
        expectedResource.setClientId(CLIENT_ID);

        Mockito.when(clientService.getClient(CLIENT_ID)).thenReturn(client);
        PowerMockito.when(ClientResourceMapper.mapToClientResource(client)).thenReturn(expectedResource);

        ResponseEntity<ClientResource> response = classUnderTest.getClientInfo(CLIENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResource, response.getBody());
    }

    @Test
    public void testCreateClientInfo() throws SnowmanException {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        ClientResource resource = new ClientResource();
        resource.setClientId(CLIENT_ID);

        Client client = new Client();
        client.setId(CLIENT_ID);

        PowerMockito.when(ClientResourceMapper.mapToClient(resource)).thenReturn(client);
        Mockito.doNothing().when(clientService).createClient(client);

        classUnderTest.createClientInfo(resource);

        Mockito.verify(clientService, Mockito.times(1)).createClient(client);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateClientInfo_throwsRuntimeException_onSnowmanException() throws SnowmanException {
        ClientResource resource = new ClientResource();
        resource.setClientId(CLIENT_ID);

        Mockito.doThrow(new SnowmanException("Client already exists"))
            .when(clientService).createClient(Matchers.any(Client.class));

        classUnderTest.createClientInfo(resource);
    }

    @Test
    public void testUpdateClientInfo() throws SnowmanException {
        PowerMockito.mockStatic(ClientResourceMapper.class);

        ClientResource resource = new ClientResource();
        resource.setClientId(CLIENT_ID);

        Client client = new Client();
        client.setId(CLIENT_ID);

        PowerMockito.when(ClientResourceMapper.mapToClient(resource)).thenReturn(client);
        Mockito.doNothing().when(clientService).updateClient(client);

        classUnderTest.updateClientInfo(resource);

        Mockito.verify(clientService, Mockito.times(1)).updateClient(client);
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateClientInfo_throwsRuntimeException_onSnowmanException() throws SnowmanException {
        ClientResource resource = new ClientResource();
        resource.setClientId(CLIENT_ID);

        Mockito.doThrow(new SnowmanException("Update failed"))
            .when(clientService).updateClient(Matchers.any(Client.class));

        classUnderTest.updateClientInfo(resource);
    }

    @Test
    public void testDeleteClientInfo() throws SnowmanException {
        Mockito.doNothing().when(clientService).deleteClient(CLIENT_ID);

        classUnderTest.deleteClientInfo(CLIENT_ID);

        Mockito.verify(clientService, Mockito.times(1)).deleteClient(CLIENT_ID);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteClientInfo_throwsRuntimeException_onSnowmanException() throws SnowmanException {
        Mockito.doThrow(new SnowmanException("Nothing to delete"))
            .when(clientService).deleteClient(CLIENT_ID);

        classUnderTest.deleteClientInfo(CLIENT_ID);
    }
}
