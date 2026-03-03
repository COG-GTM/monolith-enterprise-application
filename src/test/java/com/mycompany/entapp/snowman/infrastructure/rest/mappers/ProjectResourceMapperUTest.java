/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.mappers;

import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import com.mycompany.entapp.snowman.domain.model.Project;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProjectResourceMapperUTest {

    @Test
    public void testMapToProject() throws Exception {
        Date dateStarted = Date.from(LocalDateTime.of(2018, 1, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date dateEnded = Date.from(LocalDateTime.of(2018, 5, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

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
    public void testMapToProjectResource() throws Exception {
        int projectId = 1;
        String projectTitle = "Project Title";
        Date dateStarted = Date.from(LocalDateTime.of(2018, 1, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date dateEnded = Date.from(LocalDateTime.of(2018, 5, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

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
