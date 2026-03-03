/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class EmployeeProjectV2UTest {

    @Test
    public void testGettersAndSetters() {
        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();
        Date dateEnded = new DateTime(2020, 1, 1, 12, 0, 0).toDate();

        EmployeeProjectV2 epv2 = new EmployeeProjectV2();
        epv2.setDateStarted(dateStarted);
        epv2.setDateEnded(dateEnded);

        assertEquals(dateStarted, epv2.getDateStarted());
        assertEquals(dateEnded, epv2.getDateEnded());
    }

    @Test
    public void testPrimaryKey() {
        EmployeeProjectId primaryKey = new EmployeeProjectId();
        Employee employee = new Employee();
        employee.setId(1);
        Project project = new Project();
        project.setId(2);
        primaryKey.setEmployee(employee);
        primaryKey.setProject(project);

        EmployeeProjectV2 epv2 = new EmployeeProjectV2();
        epv2.setPrimaryKey(primaryKey);

        assertEquals(primaryKey, epv2.getPrimaryKey());
        assertEquals(employee, epv2.getEmployee());
        assertEquals(project, epv2.getProject());
    }

    @Test
    public void testSetEmployee() {
        Employee employee = new Employee();
        employee.setId(1);

        EmployeeProjectV2 epv2 = new EmployeeProjectV2();
        epv2.setEmployee(employee);

        assertEquals(employee, epv2.getEmployee());
    }

    @Test
    public void testSetProject() {
        Project project = new Project();
        project.setId(1);

        EmployeeProjectV2 epv2 = new EmployeeProjectV2();
        epv2.setProject(project);

        assertEquals(project, epv2.getProject());
    }
}
