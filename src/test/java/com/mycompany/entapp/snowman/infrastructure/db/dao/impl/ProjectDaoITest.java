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
@ContextConfiguration(locations = {"classpath:spring/test-applicationContext.xml"})
@Transactional
public class ProjectDaoITest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Client createAndSaveClient(String name) {
        Client client = new Client();
        client.setClientName(name);
        sessionFactory.getCurrentSession().save(client);
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

        int projectId = project.getId();
        Project found = projectDao.retrieveProject(projectId);

        assertNotNull(found);
        assertEquals("Test Project", found.getProjectTitle());
        assertNotNull(found.getDateStarted());
        assertNotNull(found.getClient());
        assertEquals("Test Client", found.getClient().getClientName());
    }

    @Test
    public void testRetrieveProjectReturnsNullForNonExistent() {
        Project found = projectDao.retrieveProject(99999);
        assertNull(found);
    }

    @Test
    public void testSaveProjectWithDates() {
        Client client = createAndSaveClient("Dates Client");

        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L); // +1 day

        Project project = new Project();
        project.setProjectTitle("Dated Project");
        project.setDateStarted(startDate);
        project.setDateEnded(endDate);
        project.setClient(client);

        projectDao.saveProject(project);
        sessionFactory.getCurrentSession().flush();

        int projectId = project.getId();
        Project found = projectDao.retrieveProject(projectId);

        assertNotNull(found);
        assertEquals("Dated Project", found.getProjectTitle());
        assertNotNull(found.getDateStarted());
        assertNotNull(found.getDateEnded());
    }

    @Test
    public void testUpdateProject() {
        Client client = createAndSaveClient("Update Client");

        Project project = new Project();
        project.setProjectTitle("Original Title");
        project.setDateStarted(new Date());
        project.setClient(client);

        projectDao.saveProject(project);
        sessionFactory.getCurrentSession().flush();

        int projectId = project.getId();

        Project toUpdate = projectDao.retrieveProject(projectId);
        toUpdate.setProjectTitle("Updated Title");
        sessionFactory.getCurrentSession().update(toUpdate);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Project updated = projectDao.retrieveProject(projectId);
        assertNotNull(updated);
        assertEquals("Updated Title", updated.getProjectTitle());
    }

    @Test
    public void testSaveMultipleProjects() {
        Client client = createAndSaveClient("Multi Client");

        Project project1 = new Project();
        project1.setProjectTitle("Project One");
        project1.setDateStarted(new Date());
        project1.setClient(client);
        projectDao.saveProject(project1);

        Project project2 = new Project();
        project2.setProjectTitle("Project Two");
        project2.setDateStarted(new Date());
        project2.setClient(client);
        projectDao.saveProject(project2);

        sessionFactory.getCurrentSession().flush();

        assertNotNull(projectDao.retrieveProject(project1.getId()));
        assertNotNull(projectDao.retrieveProject(project2.getId()));
        assertEquals("Project One", projectDao.retrieveProject(project1.getId()).getProjectTitle());
        assertEquals("Project Two", projectDao.retrieveProject(project2.getId()).getProjectTitle());
    }
}
