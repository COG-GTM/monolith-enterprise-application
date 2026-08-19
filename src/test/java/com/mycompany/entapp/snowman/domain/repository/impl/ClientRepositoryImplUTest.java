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
    private ClientRepositoryImpl systemUnderTest = new ClientRepositoryImpl();

    @Test
    public void testGetClient() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client);

        assertEquals(client, systemUnderTest.getClient(CLIENT_ID));
    }

    @Test
    public void testCreateClient() {
        Client client = new Client();

        systemUnderTest.createClient(client);

        Mockito.verify(clientDao).saveClient(client);
    }

    @Test
    public void testUpdateClient() {
        Client client = new Client();

        systemUnderTest.updateClient(client);

        Mockito.verify(clientDao).saveClient(client);
    }

    @Test
    public void testDeleteClient() {
        systemUnderTest.deleteClient(CLIENT_ID);

        Mockito.verify(clientDao).removeClient(CLIENT_ID);
    }
}
