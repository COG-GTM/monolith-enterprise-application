/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ProjectDao;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProjectDaoImplITest extends BaseDAOIntegrationTest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveProject() {
        Client client = createAndSaveClient("Project Client");

        Project project = createProject("Test Project", client);
        projectDao.saveProject(project);

        sessionFactory.getCurrentSession().flush();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull(retrieved);
        assertEquals("Test Project", retrieved.getProjectTitle());
        assertEquals(client.getId(), retrieved.getClient().getId());
    }

    @Test
    public void testRetrieveProject() {
        Client client = createAndSaveClient("Retrieve Client");

        Project project = createProject("Retrieve Project", client);
        projectDao.saveProject(project);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull(retrieved);
        assertEquals("Retrieve Project", retrieved.getProjectTitle());
    }

    @Test
    public void testRetrieveProjectNotFound() {
        Project retrieved = projectDao.retrieveProject(99999);
        assertNull(retrieved);
    }

    @Test
    public void testSaveMultipleProjects() {
        Client client = createAndSaveClient("Multi Client");

        Project project1 = createProject("Project One", client);
        Project project2 = createProject("Project Two", client);
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

    private Client createAndSaveClient(String name) {
        Client client = new Client();
        client.setClientName(name);
        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();
        return client;
    }

    private Project createProject(String title, Client client) {
        Project project = new Project();
        project.setProjectTitle(title);
        project.setDateStarted(new Date());
        project.setClient(client);
        return project;
    }
}
