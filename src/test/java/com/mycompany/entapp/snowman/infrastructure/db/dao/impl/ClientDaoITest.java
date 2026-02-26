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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/test-applicationContext.xml"})
@Transactional
public class ClientDaoITest {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveAndGetClient() {
        Client client = new Client();
        client.setClientName("Acme Corp");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        int clientId = client.getId();
        Client found = clientDao.getClient(clientId);

        assertNotNull(found);
        assertEquals("Acme Corp", found.getClientName());
    }

    @Test
    public void testGetClientReturnsNullForNonExistent() {
        Client found = clientDao.getClient(99999);
        assertNull(found);
    }

    @Test
    public void testUpdateClient() {
        Client client = new Client();
        client.setClientName("Original Name");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        int clientId = client.getId();

        Client toUpdate = clientDao.getClient(clientId);
        toUpdate.setClientName("Updated Name");
        sessionFactory.getCurrentSession().update(toUpdate);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Client updated = clientDao.getClient(clientId);
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getClientName());
    }

    @Test
    public void testRemoveClient() {
        Client client = new Client();
        client.setClientName("To Delete");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        int clientId = client.getId();
        assertNotNull(clientDao.getClient(clientId));

        clientDao.removeClient(clientId);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Client deleted = clientDao.getClient(clientId);
        assertNull(deleted);
    }

    @Test
    public void testSaveMultipleClients() {
        Client client1 = new Client();
        client1.setClientName("Client One");
        clientDao.saveClient(client1);

        Client client2 = new Client();
        client2.setClientName("Client Two");
        clientDao.saveClient(client2);

        sessionFactory.getCurrentSession().flush();

        assertNotNull(clientDao.getClient(client1.getId()));
        assertNotNull(clientDao.getClient(client2.getId()));
        assertEquals("Client One", clientDao.getClient(client1.getId()).getClientName());
        assertEquals("Client Two", clientDao.getClient(client2.getId()).getClientName());
    }
}
