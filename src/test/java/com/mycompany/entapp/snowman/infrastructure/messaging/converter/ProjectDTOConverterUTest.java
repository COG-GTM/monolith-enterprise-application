/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ProjectDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ProjectDTOConverterUTest {

    @Test
    public void testConvertToProjectDTO() {
        Date dateStarted = new Date();
        Date dateEnded = new Date();

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(dateStarted);
        project.setDateEnded(dateEnded);

        ProjectDTO projectDTO = ProjectDTOConverter.convertToProjectDTO(project);

        assertEquals(1, projectDTO.getProjectId());
        assertEquals("Test Project", projectDTO.getProjectTitle());
        assertEquals(dateStarted, projectDTO.getDateStarted());
        assertEquals(dateEnded, projectDTO.getDateEnded());
    }

    @Test
    public void testConvertToProjectDTOS() {
        Date dateStarted1 = new Date();
        Date dateEnded1 = new Date();
        Date dateStarted2 = new Date();
        Date dateEnded2 = new Date();

        Project project1 = new Project();
        project1.setId(1);
        project1.setProjectTitle("Project One");
        project1.setDateStarted(dateStarted1);
        project1.setDateEnded(dateEnded1);

        Project project2 = new Project();
        project2.setId(2);
        project2.setProjectTitle("Project Two");
        project2.setDateStarted(dateStarted2);
        project2.setDateEnded(dateEnded2);

        Set<Project> projects = new HashSet<Project>();
        projects.add(project1);
        projects.add(project2);

        Set<ProjectDTO> projectDTOS = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertEquals(2, projectDTOS.size());
    }

    @Test
    public void testConvertToProjectDTOSWithEmptySet() {
        Set<Project> projects = Collections.emptySet();

        Set<ProjectDTO> projectDTOS = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertTrue(projectDTOS.isEmpty());
    }
}
