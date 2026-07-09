/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.infrastructure.db.dao.HibernateH2TestSupport;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration test for {@link ClientDaoImpl} exercising real Hibernate persistence
 * against an in-memory H2 database.
 */
public class ClientDaoImplITest {

    private SessionFactory sessionFactory;
    private ClientDaoImpl systemUnderTest;

    @Before
    public void setUp() {
        sessionFactory = HibernateH2TestSupport.buildSessionFactory();
        systemUnderTest = new ClientDaoImpl();
        HibernateH2TestSupport.injectSessionFactory(systemUnderTest, sessionFactory);
    }

    @After
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void testSaveClientThenGetClientShouldReturnPersistedClient() {
        Client client = new Client();
        client.setClientName("Acme Corp");

        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        systemUnderTest.saveClient(client);
        tx.commit();

        int generatedId = client.getId();

        Transaction readTx = sessionFactory.getCurrentSession().beginTransaction();
        Client persisted = systemUnderTest.getClient(generatedId);
        readTx.commit();

        assertNotNull(persisted);
        assertEquals(generatedId, persisted.getId());
        assertEquals("Acme Corp", persisted.getClientName());
    }

    @Test
    public void testRemoveClientShouldDeleteClient() {
        Client client = new Client();
        client.setClientName("To Be Deleted");

        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        systemUnderTest.saveClient(client);
        tx.commit();

        int generatedId = client.getId();

        Transaction deleteTx = sessionFactory.getCurrentSession().beginTransaction();
        systemUnderTest.removeClient(generatedId);
        deleteTx.commit();

        Transaction verifyTx = sessionFactory.getCurrentSession().beginTransaction();
        Client deleted = systemUnderTest.getClient(generatedId);
        verifyTx.commit();

        assertNull(deleted);
    }
}
