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

public class EmployeeProjectUTest {

    @Test
    public void testGettersAndSetters() {
        Employee employee = new Employee();
        employee.setId(1);

        Project project = new Project();
        project.setId(2);

        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();
        Date dateEnded = new DateTime(2020, 1, 1, 12, 0, 0).toDate();

        EmployeeProject ep = new EmployeeProject();
        ep.setEmployee(employee);
        ep.setProject(project);
        ep.setDateStarted(dateStarted);
        ep.setDateEnded(dateEnded);

        assertEquals(employee, ep.getEmployee());
        assertEquals(project, ep.getProject());
        assertEquals(dateStarted, ep.getDateStarted());
        assertEquals(dateEnded, ep.getDateEnded());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        EmployeeProject ep = new EmployeeProject();
        assertTrue(ep.equals(ep));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        Employee employee = new Employee();
        employee.setId(1);

        Project project = new Project();
        project.setId(2);

        Date dateStarted = new DateTime(2018, 1, 1, 12, 0, 0).toDate();

        EmployeeProject ep1 = new EmployeeProject();
        ep1.setEmployee(employee);
        ep1.setProject(project);
        ep1.setDateStarted(dateStarted);

        EmployeeProject ep2 = new EmployeeProject();
        ep2.setEmployee(employee);
        ep2.setProject(project);
        ep2.setDateStarted(dateStarted);

        assertTrue(ep1.equals(ep2));
        assertEquals(ep1.hashCode(), ep2.hashCode());
    }

    @Test
    public void testEquals_Null() {
        EmployeeProject ep = new EmployeeProject();
        assertFalse(ep.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        EmployeeProject ep = new EmployeeProject();
        assertFalse(ep.equals("not an EmployeeProject"));
    }

    @Test
    public void testToString() {
        EmployeeProject ep = new EmployeeProject();
        String result = ep.toString();
        assertNotNull(result);
    }
}
