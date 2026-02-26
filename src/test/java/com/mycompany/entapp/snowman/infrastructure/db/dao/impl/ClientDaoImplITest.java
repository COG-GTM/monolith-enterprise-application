/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
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

public class ClientDaoImplITest extends BaseDAOIntegrationTest {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveClient() {
        Client client = createClient("Test Client");
        clientDao.saveClient(client);

        sessionFactory.getCurrentSession().flush();

        Client retrieved = clientDao.getClient(client.getId());
        assertNotNull(retrieved);
        assertEquals("Test Client", retrieved.getClientName());
    }

    @Test
    public void testGetClient() {
        Client client = createClient("Retrieve Client");
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
        Client client = createClient("Delete Client");
        clientDao.saveClient(client);

        sessionFactory.getCurrentSession().flush();

        int clientId = client.getId();

        clientDao.removeClient(clientId);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Client deleted = clientDao.getClient(clientId);
        assertNull(deleted);
    }

    @Test
    public void testSaveAndUpdateClient() {
        Client client = createClient("Original Name");
        clientDao.saveClient(client);

        sessionFactory.getCurrentSession().flush();

        client.setClientName("Updated Name");

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Client updated = clientDao.getClient(client.getId());
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getClientName());
    }

    private Client createClient(String name) {
        Client client = new Client();
        client.setClientName(name);
        return client;
    }
}
