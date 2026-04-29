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

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ClientRepositoryImplUTest {

    private static final int CLIENT_ID = 1;

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientRepositoryImpl classUnderTest = new ClientRepositoryImpl();

    @Test
    public void givenClientId_whenGetClient_thenReturnClientWithThatId() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName("ACME");

        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client);

        Client actual = classUnderTest.getClient(CLIENT_ID);

        Mockito.verify(clientDao, Mockito.times(1)).getClient(CLIENT_ID);
        assertEquals(client, actual);
    }

    @Test
    public void givenClient_whenCreateClient_thenSaveClient() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName("ACME");

        classUnderTest.createClient(client);

        Mockito.verify(clientDao, Mockito.times(1)).saveClient(client);
    }

    @Test
    public void givenClient_whenUpdateClient_thenSaveClient() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName("ACME-Updated");

        classUnderTest.updateClient(client);

        Mockito.verify(clientDao, Mockito.times(1)).saveClient(client);
    }

    @Test
    public void givenClientId_whenDeleteClient_thenRemoveClient() {
        classUnderTest.deleteClient(CLIENT_ID);

        Mockito.verify(clientDao, Mockito.times(1)).removeClient(CLIENT_ID);
    }
}
