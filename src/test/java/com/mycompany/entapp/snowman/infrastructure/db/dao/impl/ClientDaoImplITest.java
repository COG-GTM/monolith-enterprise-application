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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-applicationContext.xml")
@Transactional
public class ClientDaoImplITest {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveAndGetClient() {
        Client client = new Client();
        client.setClientName("TestClient");

        clientDao.saveClient(client);
        flushAndClear();

        Client found = clientDao.getClient(client.getId());
        assertNotNull(found);
        assertEquals("TestClient", found.getClientName());
    }

    @Test
    public void testGetClient_notFound() {
        Client found = clientDao.getClient(9999);
        assertNull(found);
    }

    @Test
    public void testUpdateClient() {
        Client client = new Client();
        client.setClientName("Original");
        clientDao.saveClient(client);
        flushAndClear();

        Client found = clientDao.getClient(client.getId());
        found.setClientName("Updated");
        sessionFactory.getCurrentSession().update(found);
        flushAndClear();

        Client updated = clientDao.getClient(client.getId());
        assertEquals("Updated", updated.getClientName());
    }

    @Test
    public void testRemoveClient() {
        Client client = new Client();
        client.setClientName("ToDelete");
        clientDao.saveClient(client);
        flushAndClear();

        int clientId = client.getId();
        clientDao.removeClient(clientId);
        flushAndClear();

        Client found = clientDao.getClient(clientId);
        assertNull(found);
    }

    private void flushAndClear() {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }
}
