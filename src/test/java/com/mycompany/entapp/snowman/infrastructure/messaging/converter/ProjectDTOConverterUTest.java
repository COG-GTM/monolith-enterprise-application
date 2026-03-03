/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ProjectDTO;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ProjectDTOConverterUTest {

    @Test
    public void testConvertToProjectDTO() {
        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();
        Date dateEnded = new DateTime(2020, 1, 1, 12, 0, 0).toDate();

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(dateStarted);
        project.setDateEnded(dateEnded);

        ProjectDTO result = ProjectDTOConverter.convertToProjectDTO(project);

        assertNotNull(result);
        assertEquals(1, result.getProjectId());
        assertEquals("Test Project", result.getProjectTitle());
        assertEquals(dateStarted, result.getDateStarted());
        assertEquals(dateEnded, result.getDateEnded());
    }

    @Test
    public void testConvertToProjectDTOWithNullDates() {
        Project project = new Project();
        project.setId(2);
        project.setProjectTitle("No Dates Project");
        project.setDateStarted(null);
        project.setDateEnded(null);

        ProjectDTO result = ProjectDTOConverter.convertToProjectDTO(project);

        assertNotNull(result);
        assertEquals(2, result.getProjectId());
        assertEquals("No Dates Project", result.getProjectTitle());
        assertNull(result.getDateStarted());
        assertNull(result.getDateEnded());
    }

    @Test
    public void testConvertToProjectDTOS() {
        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();

        Project project1 = new Project();
        project1.setId(1);
        project1.setProjectTitle("Project 1");
        project1.setDateStarted(dateStarted);

        Project project2 = new Project();
        project2.setId(2);
        project2.setProjectTitle("Project 2");
        project2.setDateStarted(dateStarted);

        Set<Project> projects = new HashSet<>();
        projects.add(project1);
        projects.add(project2);

        Set<ProjectDTO> result = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testConvertToProjectDTOSEmpty() {
        Set<Project> projects = new HashSet<>();

        Set<ProjectDTO> result = ProjectDTOConverter.convertToProjectDTOS(projects);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
