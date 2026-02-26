package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
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
@ContextConfiguration(locations = {"classpath:spring/test-application-context.xml"})
@Transactional
public class ClientDaoImplITest {

    @Autowired
    private ClientDao clientDao;

    @Test
    public void testSaveAndFindClient() {
        Client client = new Client();
        client.setClientName("Acme Corp");

        clientDao.saveClient(client);

        int clientId = client.getId();
        Client found = clientDao.getClient(clientId);

        assertNotNull(found);
        assertEquals("Acme Corp", found.getClientName());
    }

    @Test
    public void testUpdateClient() {
        Client client = new Client();
        client.setClientName("Original Name");
        clientDao.saveClient(client);

        int clientId = client.getId();
        Client found = clientDao.getClient(clientId);
        found.setClientName("Updated Name");
        clientDao.saveClient(found);

        Client updated = clientDao.getClient(clientId);
        assertEquals("Updated Name", updated.getClientName());
    }

    @Test
    public void testDeleteClient() {
        Client client = new Client();
        client.setClientName("ToDelete Corp");
        clientDao.saveClient(client);

        int clientId = client.getId();
        assertNotNull(clientDao.getClient(clientId));

        clientDao.removeClient(clientId);

        Client deleted = clientDao.getClient(clientId);
        assertNull(deleted);
    }

    @Test
    public void testFindNonExistentClient() {
        Client found = clientDao.getClient(99999);
        assertNull(found);
    }
}
