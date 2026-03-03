/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.repository.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class ClientRepositoryImplUTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientRepositoryImpl classUnderTest = new ClientRepositoryImpl();

    @Test
    public void testGetClient() {
        int clientId = 1;
        Client expectedClient = new Client();
        expectedClient.setId(clientId);
        expectedClient.setClientName("TestClient");

        Mockito.when(clientDao.getClient(clientId)).thenReturn(expectedClient);

        Client actualClient = classUnderTest.getClient(clientId);

        assertNotNull(actualClient);
        assertEquals(expectedClient, actualClient);
        Mockito.verify(clientDao, times(1)).getClient(clientId);
    }

    @Test
    public void testGetClientReturnsNull() {
        int clientId = 999;

        Mockito.when(clientDao.getClient(clientId)).thenReturn(null);

        Client actualClient = classUnderTest.getClient(clientId);

        assertNull(actualClient);
        Mockito.verify(clientDao, times(1)).getClient(clientId);
    }

    @Test
    public void testCreateClient() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("NewClient");

        Mockito.doNothing().when(clientDao).saveClient(client);

        classUnderTest.createClient(client);

        Mockito.verify(clientDao, times(1)).saveClient(client);
    }

    @Test
    public void testUpdateClient() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("UpdatedClient");

        Mockito.doNothing().when(clientDao).saveClient(client);

        classUnderTest.updateClient(client);

        Mockito.verify(clientDao, times(1)).saveClient(client);
    }

    @Test
    public void testDeleteClient() {
        int clientId = 1;

        Mockito.doNothing().when(clientDao).removeClient(clientId);

        classUnderTest.deleteClient(clientId);

        Mockito.verify(clientDao, times(1)).removeClient(clientId);
    }
}
