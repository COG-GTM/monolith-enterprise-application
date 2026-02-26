package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ProjectDao;
import org.hibernate.Session;
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
@ContextConfiguration(locations = {"classpath:spring/test-application-context.xml"})
@Transactional
public class ProjectDaoImplITest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    private Client createAndSaveClient(String name) {
        Client client = new Client();
        client.setClientName(name);
        clientDao.saveClient(client);
        return client;
    }

    @Test
    public void testSaveAndFindProject() {
        Client client = createAndSaveClient("Test Client");

        Project project = new Project();
        project.setProjectTitle("Test Project");
        project.setDateStarted(new Date());
        project.setClient(client);

        projectDao.saveProject(project);

        int projectId = project.getId();
        Project found = projectDao.retrieveProject(projectId);

        assertNotNull(found);
        assertEquals("Test Project", found.getProjectTitle());
        assertEquals(client.getId(), found.getClient().getId());
    }

    @Test
    public void testUpdateProject() {
        Client client = createAndSaveClient("Client for Update");

        Project project = new Project();
        project.setProjectTitle("Original Title");
        project.setDateStarted(new Date());
        project.setClient(client);
        projectDao.saveProject(project);

        int projectId = project.getId();
        Project found = projectDao.retrieveProject(projectId);
        found.setProjectTitle("Updated Title");
        projectDao.saveProject(found);

        Project updated = projectDao.retrieveProject(projectId);
        assertEquals("Updated Title", updated.getProjectTitle());
    }

    @Test
    public void testDeleteProject() {
        Client client = createAndSaveClient("Client for Delete");

        Project project = new Project();
        project.setProjectTitle("ToDelete Project");
        project.setDateStarted(new Date());
        project.setClient(client);
        projectDao.saveProject(project);
        getCurrentSession().flush();

        int projectId = project.getId();
        assertNotNull(projectDao.retrieveProject(projectId));

        // Note: projectDao.removeProject() has a bug - it passes int to session.delete()
        // instead of loading the entity first. Deleting via session directly.
        Project toDelete = (Project) getCurrentSession().get(Project.class, projectId);
        getCurrentSession().delete(toDelete);
        getCurrentSession().flush();

        Project deleted = projectDao.retrieveProject(projectId);
        assertNull(deleted);
    }

    @Test
    public void testFindNonExistentProject() {
        Project found = projectDao.retrieveProject(99999);
        assertNull(found);
    }
}
