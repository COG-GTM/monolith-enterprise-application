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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectDTOConverterUTest {

    @Test
    public void convertToProjectDTO_copiesFields() {
        Project project = ProjectTestHelper.getProject();

        ProjectDTO dto = ProjectDTOConverter.convertToProjectDTO(project);

        assertNotNull(dto);
        assertEquals(project.getId(), dto.getProjectId());
        assertEquals(project.getProjectTitle(), dto.getProjectTitle());
        assertEquals(project.getDateStarted(), dto.getDateStarted());
        assertEquals(project.getDateEnded(), dto.getDateEnded());
    }

    @Test
    public void convertToProjectDTOS_convertsAllProjectsInSet() {
        Set<Project> projects = new HashSet<Project>();
        projects.add(ProjectTestHelper.getProject());

        Set<ProjectDTO> dtos = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
    }

    @Test
    public void convertToProjectDTOS_handlesEmptySet() {
        Set<ProjectDTO> dtos = ProjectDTOConverter.convertToProjectDTOS(new HashSet<Project>());

        assertNotNull(dtos);
        assertEquals(0, dtos.size());
    }
}
