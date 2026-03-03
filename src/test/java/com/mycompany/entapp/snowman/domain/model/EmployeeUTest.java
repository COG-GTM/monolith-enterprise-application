/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class EmployeeUTest {

    @Test
    public void testGettersAndSetters() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        Set<EmployeeProject> projects = new HashSet<>();

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);
        employee.setProjects(projects);

        assertEquals(1, employee.getId());
        assertEquals("John", employee.getFirstname());
        assertEquals("Doe", employee.getSurname());
        assertEquals(role, employee.getRole());
        assertEquals(projects, employee.getProjects());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");

        assertTrue(employee.equals(employee));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");

        Employee emp1 = new Employee();
        emp1.setId(1);
        emp1.setFirstname("John");
        emp1.setSurname("Doe");
        emp1.setRole(role);

        Employee emp2 = new Employee();
        emp2.setId(1);
        emp2.setFirstname("John");
        emp2.setSurname("Doe");
        emp2.setRole(role);

        assertTrue(emp1.equals(emp2));
        assertEquals(emp1.hashCode(), emp2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentId() {
        Employee emp1 = new Employee();
        emp1.setId(1);
        emp1.setFirstname("John");
        emp1.setSurname("Doe");

        Employee emp2 = new Employee();
        emp2.setId(2);
        emp2.setFirstname("John");
        emp2.setSurname("Doe");

        assertFalse(emp1.equals(emp2));
    }

    @Test
    public void testEquals_Null() {
        Employee employee = new Employee();
        assertFalse(employee.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        Employee employee = new Employee();
        assertFalse(employee.equals("not an employee"));
    }

    @Test
    public void testToString() {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);

        String result = employee.toString();
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("firstname=John"));
        assertTrue(result.contains("surname=Doe"));
    }
}
