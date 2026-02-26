/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ClientDaoITest extends BaseDaoITest {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveClient() {
        Client client = new Client();
        client.setClientName("Test Client");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        int generatedId = client.getId();
        Client retrieved = clientDao.getClient(generatedId);
        assertNotNull(retrieved);
        assertEquals("Test Client", retrieved.getClientName());
    }

    @Test
    public void testGetClient() {
        Client client = new Client();
        client.setClientName("Retrieve Client");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Client retrieved = clientDao.getClient(client.getId());
        assertNotNull(retrieved);
        assertEquals("Retrieve Client", retrieved.getClientName());
    }

    @Test
    public void testGetClientNotFound() {
        Client retrieved = clientDao.getClient(99999);
        assertNull(retrieved);
    }

    @Test
    public void testRemoveClient() {
        Client client = new Client();
        client.setClientName("Delete Client");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        int clientId = client.getId();
        assertNotNull(clientDao.getClient(clientId));

        clientDao.removeClient(clientId);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertNull(clientDao.getClient(clientId));
    }

    @Test
    public void testSaveMultipleClients() {
        Client client1 = new Client();
        client1.setClientName("Client One");

        Client client2 = new Client();
        client2.setClientName("Client Two");

        clientDao.saveClient(client1);
        clientDao.saveClient(client2);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Client retrieved1 = clientDao.getClient(client1.getId());
        Client retrieved2 = clientDao.getClient(client2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Client One", retrieved1.getClientName());
        assertEquals("Client Two", retrieved2.getClientName());
    }
}
