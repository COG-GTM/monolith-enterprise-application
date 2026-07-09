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

@RunWith(MockitoJUnitRunner.class)
public class ClientRepositoryImplUTest {

    private static final int CLIENT_ID = 1;

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientRepositoryImpl sut = new ClientRepositoryImpl();

    @Test
    public void testGetClient() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName("Acme");

        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client);

        Client actualClient = sut.getClient(CLIENT_ID);

        assertEquals(client, actualClient);
        Mockito.verify(clientDao, Mockito.times(1)).getClient(CLIENT_ID);
    }

    @Test
    public void testCreateClient() {
        Client client = new Client();

        Mockito.doNothing().when(clientDao).saveClient(client);

        sut.createClient(client);

        Mockito.verify(clientDao, Mockito.times(1)).saveClient(client);
    }

    @Test
    public void testUpdateClient() {
        Client client = new Client();

        Mockito.doNothing().when(clientDao).saveClient(client);

        sut.updateClient(client);

        Mockito.verify(clientDao, Mockito.times(1)).saveClient(client);
    }

    @Test
    public void testDeleteClient() {
        Mockito.doNothing().when(clientDao).removeClient(CLIENT_ID);

        sut.deleteClient(CLIENT_ID);

        Mockito.verify(clientDao, Mockito.times(1)).removeClient(CLIENT_ID);
    }
}
