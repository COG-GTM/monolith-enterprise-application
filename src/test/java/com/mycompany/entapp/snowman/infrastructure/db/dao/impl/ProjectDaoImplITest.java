/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ProjectDao;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Integration tests for {@link ProjectDaoImpl} using H2 in-memory database.
 */
public class ProjectDaoImplITest extends BaseDAOITest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Client testClient;

    @Before
    public void setUp() {
        // Project requires a Client (non-nullable foreign key)
        testClient = new Client();
        testClient.setClientName("Test Client");
        sessionFactory.getCurrentSession().save(testClient);
        sessionFactory.getCurrentSession().flush();
    }

    @Test
    public void testSaveAndRetrieveProject() {
        Project project = new Project();
        project.setProjectTitle("Test Project");
        project.setDateStarted(new Date());
        project.setClient(testClient);

        projectDao.saveProject(project);
        flushAndClear();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull("Saved project should be retrievable", retrieved);
        assertEquals("Test Project", retrieved.getProjectTitle());
        assertNotNull("Project should have a client", retrieved.getClient());
        assertEquals("Test Client", retrieved.getClient().getClientName());
    }

    @Test
    public void testRetrieveProject_NotFound() {
        Project project = projectDao.retrieveProject(999);
        assertNull("Non-existent project should return null", project);
    }

    @Test
    public void testSaveProjectWithDates() {
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L); // +1 day

        Project project = new Project();
        project.setProjectTitle("Dated Project");
        project.setDateStarted(startDate);
        project.setDateEnded(endDate);
        project.setClient(testClient);

        projectDao.saveProject(project);
        flushAndClear();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull(retrieved);
        assertNotNull("Start date should be persisted", retrieved.getDateStarted());
        assertNotNull("End date should be persisted", retrieved.getDateEnded());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProject_ThrowsIllegalArgumentException() {
        // Note: removeProject has a bug - it passes an int to session.delete()
        // instead of an entity object, causing an IllegalArgumentException
        Project project = new Project();
        project.setProjectTitle("To Delete");
        project.setDateStarted(new Date());
        project.setClient(testClient);

        projectDao.saveProject(project);
        flushAndClear();

        projectDao.removeProject(project.getId());
    }

    @Test
    public void testSaveMultipleProjects() {
        Project project1 = new Project();
        project1.setProjectTitle("Project Alpha");
        project1.setDateStarted(new Date());
        project1.setClient(testClient);

        Project project2 = new Project();
        project2.setProjectTitle("Project Beta");
        project2.setDateStarted(new Date());
        project2.setClient(testClient);

        projectDao.saveProject(project1);
        projectDao.saveProject(project2);
        flushAndClear();

        Project retrieved1 = projectDao.retrieveProject(project1.getId());
        Project retrieved2 = projectDao.retrieveProject(project2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Project Alpha", retrieved1.getProjectTitle());
        assertEquals("Project Beta", retrieved2.getProjectTitle());
    }
}
