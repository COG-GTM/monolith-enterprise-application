/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EmployeeUTest {

    @Test
    public void gettersAndSetters_storeValues() {
        EmployeeRole role = new EmployeeRole();
        role.setId(2);
        role.setRole("Developer");

        Set<EmployeeProject> projects = new HashSet<EmployeeProject>();

        Employee employee = new Employee();
        employee.setId(10);
        employee.setFirstname("Alice");
        employee.setSurname("Smith");
        employee.setRole(role);
        employee.setProjects(projects);

        assertEquals(10, employee.getId());
        assertEquals("Alice", employee.getFirstname());
        assertEquals("Smith", employee.getSurname());
        assertEquals(role, employee.getRole());
        assertEquals(projects, employee.getProjects());
    }

    @Test
    public void equalsAndHashCode_followContract() {
        Employee a = new Employee();
        a.setId(1);
        a.setFirstname("Alice");
        a.setSurname("Smith");

        Employee b = new Employee();
        b.setId(1);
        b.setFirstname("Alice");
        b.setSurname("Smith");

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("not an employee"));

        b.setSurname("Jones");
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("Alice");
        employee.setProjects(Collections.<EmployeeProject>emptySet());
        assertNotNull(employee.toString());
    }
}
