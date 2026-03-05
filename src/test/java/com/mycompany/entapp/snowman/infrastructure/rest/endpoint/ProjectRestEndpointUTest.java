/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.ProjectResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProjectRestEndpointUTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectRestEndpoint classUnderTest = new ProjectRestEndpoint();

    @Test
    public void testCreateProject() {

        ProjectResource projectResource = new ProjectResource();
        projectResource.setProjectId(1);
        projectResource.setTitle("Title");
        projectResource.setDateStarted(new DateTime(2018, 1,1, 12, 0, 0).toDate());

        Mockito.doNothing().when(projectService).createProject(ArgumentMatchers.any(Project.class));

        ResponseEntity<?> response = classUnderTest.createProject(projectResource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetProject() {
        Integer projectId = 1;

        Project project = new Project();

        ProjectResource expectedProjectResource = new ProjectResource();

        try (MockedStatic<ProjectResourceMapper> mockedMapper = Mockito.mockStatic(ProjectResourceMapper.class)) {
            mockedMapper.when(() -> ProjectResourceMapper.mapToProjectResource(project)).thenReturn(expectedProjectResource);
            Mockito.when(projectService.getProject(projectId)).thenReturn(project);

            ResponseEntity response = classUnderTest.getProject(projectId);

            assertEquals(expectedProjectResource, response.getBody());
        }
    }

    @Test
    public void testDeleteProject() {
        int projectId = 1;

        Mockito.doNothing().when(projectService).deleteProject(projectId);

        classUnderTest.deleteProject(projectId);

        Mockito.verify(projectService, Mockito.times(1)).deleteProject(projectId);
    }

    @Test
    public void testUpdateProject() {
        ProjectResource projectResource = new ProjectResource();
        projectResource.setProjectId(1);
        projectResource.setTitle("Title");
        projectResource.setDateStarted(new DateTime(2018, 1,1, 12, 0, 0).toDate());

        Mockito.doNothing().when(projectService).createProject(ArgumentMatchers.any(Project.class));

        ResponseEntity<?> response = classUnderTest.updateProject(projectResource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
