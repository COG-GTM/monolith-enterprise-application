/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ProjectDao;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
@Transactional
public class ProjectDaoImplITest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Client createAndSaveClient(String name) {
        Client client = new Client();
        client.setClientName(name);
        clientDao.saveClient(client);
        sessionFactory.getCurrentSession().flush();
        return client;
    }

    @Test
    public void testSaveAndRetrieveProject() {
        Client client = createAndSaveClient("Test Client");

        Project project = new Project();
        project.setProjectTitle("Test Project");
        project.setDateStarted(new Date());
        project.setClient(client);

        projectDao.saveProject(project);
        sessionFactory.getCurrentSession().flush();

        Project retrieved = projectDao.retrieveProject(project.getId());
        assertNotNull(retrieved);
        assertEquals("Test Project", retrieved.getProjectTitle());
        assertEquals(client.getId(), retrieved.getClient().getId());
    }

    @Test
    public void testRetrieveProject_notFound() {
        Project retrieved = projectDao.retrieveProject(9999);
        assertNull(retrieved);
    }

    @Test
    public void testSaveMultipleProjects() {
        Client client = createAndSaveClient("Multi Project Client");

        Project project1 = new Project();
        project1.setProjectTitle("Project Alpha");
        project1.setDateStarted(new Date());
        project1.setClient(client);

        Project project2 = new Project();
        project2.setProjectTitle("Project Beta");
        project2.setDateStarted(new Date());
        project2.setClient(client);

        projectDao.saveProject(project1);
        projectDao.saveProject(project2);
        sessionFactory.getCurrentSession().flush();

        Project retrieved1 = projectDao.retrieveProject(project1.getId());
        Project retrieved2 = projectDao.retrieveProject(project2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Project Alpha", retrieved1.getProjectTitle());
        assertEquals("Project Beta", retrieved2.getProjectTitle());
    }
}
