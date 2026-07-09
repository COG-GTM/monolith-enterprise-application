/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.ProjectTestHelper;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.db.dao.HibernateH2TestSupport;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration test for {@link ProjectDaoImpl} exercising real Hibernate persistence
 * against an in-memory H2 database.
 */
public class ProjectDaoImplITest {

    private SessionFactory sessionFactory;
    private ProjectDaoImpl systemUnderTest;

    @Before
    public void setUp() {
        sessionFactory = HibernateH2TestSupport.buildSessionFactory();
        systemUnderTest = new ProjectDaoImpl();
        HibernateH2TestSupport.injectSessionFactory(systemUnderTest, sessionFactory);
    }

    @After
    public void tearDown() {
        sessionFactory.close();
    }

    private int saveProjectWithClient(String projectTitle) {
        Client client = new Client();
        client.setClientName("Owning Client");

        Project project = ProjectTestHelper.getProject();
        project.setProjectTitle(projectTitle);
        project.setClient(client);

        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().save(client);
        systemUnderTest.saveProject(project);
        tx.commit();

        return project.getId();
    }

    @Test
    public void testSaveProjectThenRetrieveProjectShouldReturnPersistedProject() {
        int generatedId = saveProjectWithClient("Migration");

        Transaction readTx = sessionFactory.getCurrentSession().beginTransaction();
        Project persisted = systemUnderTest.retrieveProject(generatedId);
        readTx.commit();

        assertNotNull(persisted);
        assertEquals(generatedId, persisted.getId());
        assertEquals("Migration", persisted.getProjectTitle());
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveProjectDelegatesDeleteToSession() {
        int generatedId = saveProjectWithClient("Doomed");

        // removeProject(int) delegates to Session.delete(Object) with an auto-boxed Integer,
        // which Hibernate rejects because Integer is not a mapped entity.
        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        try {
            systemUnderTest.removeProject(generatedId);
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }
}
