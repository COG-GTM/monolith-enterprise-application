/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ProjectUTest {

    @Test
    public void testGettersAndSetters() {
        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();
        Date dateEnded = new DateTime(2020, 1, 1, 12, 0, 0).toDate();
        Client client = new Client();
        client.setId(1);
        Set<EmployeeProject> employeeProjects = new HashSet<>();

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(dateStarted);
        project.setDateEnded(dateEnded);
        project.setClient(client);
        project.setEmployeeProjects(employeeProjects);

        assertEquals(1, project.getId());
        assertEquals("Test Project", project.getProjectTitle());
        assertEquals(dateStarted, project.getDateStarted());
        assertEquals(dateEnded, project.getDateEnded());
        assertEquals(client, project.getClient());
        assertEquals(employeeProjects, project.getEmployeeProjects());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        Project project = new Project();
        project.setId(1);

        assertTrue(project.equals(project));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();
        Client client = new Client();
        client.setId(1);

        Project p1 = new Project();
        p1.setId(1);
        p1.setProjectTitle("Project");
        p1.setDateStarted(dateStarted);
        p1.setClient(client);

        Project p2 = new Project();
        p2.setId(1);
        p2.setProjectTitle("Project");
        p2.setDateStarted(dateStarted);
        p2.setClient(client);

        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentId() {
        Project p1 = new Project();
        p1.setId(1);
        p1.setProjectTitle("Project");

        Project p2 = new Project();
        p2.setId(2);
        p2.setProjectTitle("Project");

        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEquals_Null() {
        Project project = new Project();
        assertFalse(project.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        Project project = new Project();
        assertFalse(project.equals("not a project"));
    }

    @Test
    public void testToString() {
        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(dateStarted);

        String result = project.toString();
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("projectTitle=Test Project"));
    }
}
