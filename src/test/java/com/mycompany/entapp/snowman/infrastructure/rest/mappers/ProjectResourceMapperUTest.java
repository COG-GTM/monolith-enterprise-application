/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.mappers;

import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ProjectResourceMapperUTest {

    @Test
    public void testMapToProject() throws Exception {
        Date dateStarted = new DateTime(2018,1,1,0,0,0).toDate();
        Date dateEnded = new DateTime(2018,5,1,0,0,0).toDate();

        ProjectResource projectResource = new ProjectResource();
        projectResource.setProjectId(1);
        projectResource.setTitle("Project Title");
        projectResource.setDateStarted(dateStarted);
        projectResource.setDateEnded(dateEnded);

        Project mappedProject = ProjectResourceMapper.mapToProject(projectResource);

        assertEquals(1, mappedProject.getId());
        assertEquals("Project Title", mappedProject.getProjectTitle());
        assertEquals(dateStarted, mappedProject.getDateStarted());
        assertEquals(dateEnded, mappedProject.getDateEnded());
    }

    @Test
    public void testMapToProjectSetsClientReference() throws Exception {
        ProjectResource projectResource = new ProjectResource();
        projectResource.setTitle("Project Title");
        projectResource.setClientId(7);

        Project mappedProject = ProjectResourceMapper.mapToProject(projectResource);

        assertEquals(7, mappedProject.getClient().getId());
    }

    @Test
    public void testMapToProjectWithoutClientId() throws Exception {
        ProjectResource projectResource = new ProjectResource();
        projectResource.setTitle("Project Title");

        assertNull(ProjectResourceMapper.mapToProject(projectResource).getClient());
    }

    @Test
    public void testMapToProjectResourceIncludesClientId() throws Exception {
        Client client = new Client();
        client.setId(7);
        Project project = new Project();
        project.setClient(client);

        assertEquals(Integer.valueOf(7), ProjectResourceMapper.mapToProjectResource(project).getClientId());
    }

    @Test
    public void testMapToProjectResource() throws Exception {
        int projectId = 1;
        String projectTitle = "Project Title";
        Date dateStarted = new DateTime(2018,1,1,0,0,0).toDate();
        Date dateEnded = new DateTime(2018,5,1,0,0,0).toDate();

        Project project = new Project();
        project.setId(projectId);
        project.setProjectTitle(projectTitle);
        project.setDateStarted(dateStarted);
        project.setDateEnded(dateEnded);

        ProjectResource projectResource = ProjectResourceMapper.mapToProjectResource(project);

        assertEquals(projectId, projectResource.getProjectId());
        assertEquals(projectTitle, projectResource.getTitle());
        assertEquals(dateStarted, projectResource.getDateStarted());
        assertEquals(dateEnded, projectResource.getDateEnded());
    }

    @Test
    public void testMapToProjects() throws Exception {
        fail("To be Implemented");
    }

}
