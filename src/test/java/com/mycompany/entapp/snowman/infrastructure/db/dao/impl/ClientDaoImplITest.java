/*
 * |-------------------------------------------------
 * | Copyright (c) 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Integration tests for {@link ClientDaoImpl} using H2 in-memory database.
 */
public class ClientDaoImplITest extends BaseDAOITest {

    @Autowired
    private ClientDao clientDao;

    @Test
    public void testSaveAndGetClient() {
        Client client = new Client();
        client.setClientName("Acme Corp");

        clientDao.saveClient(client);
        flushAndClear();

        Client retrieved = clientDao.getClient(client.getId());
        assertNotNull("Saved client should be retrievable", retrieved);
        assertEquals("Acme Corp", retrieved.getClientName());
    }

    @Test
    public void testGetClient_NotFound() {
        Client client = clientDao.getClient(999);
        assertNull("Non-existent client should return null", client);
    }

    @Test
    public void testRemoveClient() {
        Client client = new Client();
        client.setClientName("To Delete");

        clientDao.saveClient(client);
        flushAndClear();

        int clientId = client.getId();
        assertNotNull("Client should exist before removal", clientDao.getClient(clientId));

        clientDao.removeClient(clientId);
        flushAndClear();

        Client deleted = clientDao.getClient(clientId);
        assertNull("Client should be removed", deleted);
    }

    @Test
    public void testSaveMultipleClients() {
        Client client1 = new Client();
        client1.setClientName("Client One");

        Client client2 = new Client();
        client2.setClientName("Client Two");

        clientDao.saveClient(client1);
        clientDao.saveClient(client2);
        flushAndClear();

        Client retrieved1 = clientDao.getClient(client1.getId());
        Client retrieved2 = clientDao.getClient(client2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Client One", retrieved1.getClientName());
        assertEquals("Client Two", retrieved2.getClientName());
    }
}
