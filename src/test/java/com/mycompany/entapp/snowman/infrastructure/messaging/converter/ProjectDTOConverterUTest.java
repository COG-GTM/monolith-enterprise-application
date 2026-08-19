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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProjectDTOConverterUTest {

    @Test
    public void testConvertToProjectDTO() {
        Project project = ProjectTestHelper.getProject();

        ProjectDTO projectDTO = ProjectDTOConverter.convertToProjectDTO(project);

        assertEquals(project.getId(), projectDTO.getProjectId());
        assertEquals(project.getProjectTitle(), projectDTO.getProjectTitle());
        assertEquals(project.getDateStarted(), projectDTO.getDateStarted());
        assertEquals(project.getDateEnded(), projectDTO.getDateEnded());
    }

    @Test
    public void testConvertToProjectDTOS() {
        Project project = ProjectTestHelper.getProject();
        Set<Project> projects = new HashSet<>();
        projects.add(project);

        Set<ProjectDTO> projectDTOS = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertEquals(1, projectDTOS.size());
        assertEquals(project.getId(), projectDTOS.iterator().next().getProjectId());
    }

    @Test
    public void testConvertToProjectDTOSGivenNoProjects() {
        Set<ProjectDTO> projectDTOS = ProjectDTOConverter.convertToProjectDTOS(Collections.<Project>emptySet());

        assertTrue(projectDTOS.isEmpty());
    }
}
