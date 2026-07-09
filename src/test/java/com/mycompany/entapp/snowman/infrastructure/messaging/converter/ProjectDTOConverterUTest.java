/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.ProjectTestHelper;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ProjectDTO;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ProjectDTOConverterUTest {

    @Test
    public void givenProject_whenConvertToProjectDTO_thenReturnProjectDTO() {
        Project project = ProjectTestHelper.getProject();

        ProjectDTO projectDTO = ProjectDTOConverter.convertToProjectDTO(project);

        assertEquals(project.getId(), projectDTO.getProjectId());
        assertEquals(project.getProjectTitle(), projectDTO.getProjectTitle());
        assertEquals(project.getDateStarted(), projectDTO.getDateStarted());
        assertEquals(project.getDateEnded(), projectDTO.getDateEnded());
    }

    @Test
    public void givenProjects_whenConvertToProjectDTOS_thenReturnProjectDTOSet() {
        Project project = ProjectTestHelper.getProject();
        Set<Project> projects = new HashSet<>();
        projects.add(project);

        Set<ProjectDTO> projectDTOS = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertEquals(1, projectDTOS.size());
        ProjectDTO projectDTO = projectDTOS.iterator().next();
        assertEquals(project.getId(), projectDTO.getProjectId());
        assertEquals(project.getProjectTitle(), projectDTO.getProjectTitle());
    }

    @Test
    public void givenEmptyProjects_whenConvertToProjectDTOS_thenReturnEmptySet() {
        Set<ProjectDTO> projectDTOS = ProjectDTOConverter.convertToProjectDTOS(new HashSet<Project>());

        assertTrue(projectDTOS.isEmpty());
    }
}
