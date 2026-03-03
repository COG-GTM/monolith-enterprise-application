/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmployeeProjectIdUTest {

    @Test
    public void testGettersAndSetters() {
        Employee employee = new Employee();
        employee.setId(1);

        Project project = new Project();
        project.setId(2);

        EmployeeProjectId epId = new EmployeeProjectId();
        epId.setEmployee(employee);
        epId.setProject(project);

        assertEquals(employee, epId.getEmployee());
        assertEquals(project, epId.getProject());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        EmployeeProjectId epId = new EmployeeProjectId();
        assertTrue(epId.equals(epId));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        Employee employee = new Employee();
        employee.setId(1);

        Project project = new Project();
        project.setId(2);

        EmployeeProjectId id1 = new EmployeeProjectId();
        id1.setEmployee(employee);
        id1.setProject(project);

        EmployeeProjectId id2 = new EmployeeProjectId();
        id2.setEmployee(employee);
        id2.setProject(project);

        assertTrue(id1.equals(id2));
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentEmployee() {
        Employee emp1 = new Employee();
        emp1.setId(1);

        Employee emp2 = new Employee();
        emp2.setId(2);

        Project project = new Project();
        project.setId(1);

        EmployeeProjectId id1 = new EmployeeProjectId();
        id1.setEmployee(emp1);
        id1.setProject(project);

        EmployeeProjectId id2 = new EmployeeProjectId();
        id2.setEmployee(emp2);
        id2.setProject(project);

        assertFalse(id1.equals(id2));
    }

    @Test
    public void testEquals_Null() {
        EmployeeProjectId epId = new EmployeeProjectId();
        assertFalse(epId.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        EmployeeProjectId epId = new EmployeeProjectId();
        assertFalse(epId.equals("not an id"));
    }

    @Test
    public void testToString() {
        EmployeeProjectId epId = new EmployeeProjectId();
        String result = epId.toString();
        assertNotNull(result);
    }
}
