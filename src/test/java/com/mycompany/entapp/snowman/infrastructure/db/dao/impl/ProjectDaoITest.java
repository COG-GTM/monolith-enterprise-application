/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
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

public class ProjectDaoITest extends BaseDaoITest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Client testClient;

    @Before
    public void setUp() {
        testClient = new Client();
        testClient.setClientName("Test Client");
        sessionFactory.getCurrentSession().save(testClient);
        sessionFactory.getCurrentSession().flush();
    }

    @Test
    public void testSaveProject() {
        Project project = createProject("Test Project", testClient);

        projectDao.saveProject(project);
        sessionFactory.getCurrentSession().flush();

        int generatedId = project.getId();
        Project retrieved = projectDao.retrieveProject(generatedId);
        assertNotNull(retrieved);
        assertEquals("Test Project", retrieved.getProjectTitle());
        assertNotNull(retrieved.getDateStarted());
    }

    @Test
    public void testRetrieveProject() {
        Project project = createProject("Retrieve Project", testClient);

        projectDao.saveProject(project);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull(retrieved);
        assertEquals("Retrieve Project", retrieved.getProjectTitle());
        assertNotNull(retrieved.getClient());
        assertEquals("Test Client", retrieved.getClient().getClientName());
    }

    @Test
    public void testRetrieveProjectNotFound() {
        Project retrieved = projectDao.retrieveProject(99999);
        assertNull(retrieved);
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
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull(retrieved);
        assertEquals("Dated Project", retrieved.getProjectTitle());
        assertNotNull(retrieved.getDateStarted());
        assertNotNull(retrieved.getDateEnded());
    }

    @Test
    public void testSaveMultipleProjects() {
        Project project1 = createProject("Project One", testClient);
        Project project2 = createProject("Project Two", testClient);

        projectDao.saveProject(project1);
        projectDao.saveProject(project2);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Project retrieved1 = projectDao.retrieveProject(project1.getId());
        Project retrieved2 = projectDao.retrieveProject(project2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Project One", retrieved1.getProjectTitle());
        assertEquals("Project Two", retrieved2.getProjectTitle());
    }

    private Project createProject(String title, Client client) {
        Project project = new Project();
        project.setProjectTitle(title);
        project.setDateStarted(new Date());
        project.setClient(client);
        return project;
    }
}
