/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ProjectDao;
import org.hibernate.SessionFactory;
import org.junit.Before;
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
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-applicationContext.xml")
@Transactional
public class ProjectDaoImplITest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Client testClient;

    @Before
    public void setUp() {
        testClient = new Client();
        testClient.setClientName("TestClient");
        clientDao.saveClient(testClient);
        flushAndClear();
    }

    @Test
    public void testSaveAndRetrieveProject() {
        Project project = createProject("TestProject");

        projectDao.saveProject(project);
        flushAndClear();

        Project found = projectDao.retrieveProject(project.getId());
        assertNotNull(found);
        assertEquals("TestProject", found.getProjectTitle());
        assertNotNull(found.getClient());
        assertEquals(testClient.getId(), found.getClient().getId());
    }

    @Test
    public void testRetrieveProject_notFound() {
        Project found = projectDao.retrieveProject(9999);
        assertNull(found);
    }

    @Test
    public void testSaveProject() {
        Project project = createProject("NewProject");

        projectDao.saveProject(project);
        flushAndClear();

        assertTrue(project.getId() > 0);
    }

    @Test
    public void testUpdateProject() {
        Project project = createProject("Original");
        projectDao.saveProject(project);
        flushAndClear();

        Project found = projectDao.retrieveProject(project.getId());
        found.setProjectTitle("Updated");
        sessionFactory.getCurrentSession().update(found);
        flushAndClear();

        Project updated = projectDao.retrieveProject(project.getId());
        assertEquals("Updated", updated.getProjectTitle());
    }

    @Test
    public void testDeleteProject() {
        Project project = createProject("ToDelete");
        projectDao.saveProject(project);
        flushAndClear();

        int projectId = project.getId();
        Project toDelete = projectDao.retrieveProject(projectId);
        sessionFactory.getCurrentSession().delete(toDelete);
        flushAndClear();

        Project found = projectDao.retrieveProject(projectId);
        assertNull(found);
    }

    private Project createProject(String title) {
        Project project = new Project();
        project.setProjectTitle(title);
        project.setDateStarted(new Date());
        project.setClient(testClient);
        return project;
    }

    private void flushAndClear() {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }
}
