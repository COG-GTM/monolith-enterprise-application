/*
 * |-------------------------------------------------
 * | Copyright (c) 2017 Colin But. All rights reserved.
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
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
@Transactional
public class ClientDaoImplITest {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveAndGetClient() {
        Client client = new Client();
        client.setClientName("Test Client");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        Client retrieved = clientDao.getClient(client.getId());
        assertNotNull(retrieved);
        assertEquals("Test Client", retrieved.getClientName());
    }

    @Test
    public void testGetClient_notFound() {
        Client retrieved = clientDao.getClient(9999);
        assertNull(retrieved);
    }

    @Test
    public void testRemoveClient() {
        Client client = new Client();
        client.setClientName("Client To Remove");

        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();

        int savedId = client.getId();
        assertNotNull(clientDao.getClient(savedId));

        clientDao.removeClient(savedId);
        sessionFactory.getCurrentSession().flush();

        Client removed = clientDao.getClient(savedId);
        assertNull(removed);
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

        Client retrieved1 = clientDao.getClient(client1.getId());
        Client retrieved2 = clientDao.getClient(client2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Client One", retrieved1.getClientName());
        assertEquals("Client Two", retrieved2.getClientName());
    }
}
