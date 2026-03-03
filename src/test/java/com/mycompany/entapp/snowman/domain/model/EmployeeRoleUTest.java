/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmployeeRoleUTest {

    @Test
    public void testGettersAndSetters() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        assertEquals(1, role.getId());
        assertEquals("Developer", role.getRole());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        assertTrue(role.equals(role));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        EmployeeRole role1 = new EmployeeRole();
        role1.setId(1);
        role1.setRole("Developer");

        EmployeeRole role2 = new EmployeeRole();
        role2.setId(1);
        role2.setRole("Developer");

        assertTrue(role1.equals(role2));
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentId() {
        EmployeeRole role1 = new EmployeeRole();
        role1.setId(1);
        role1.setRole("Developer");

        EmployeeRole role2 = new EmployeeRole();
        role2.setId(2);
        role2.setRole("Developer");

        assertFalse(role1.equals(role2));
    }

    @Test
    public void testEqualsAndHashCode_DifferentRole() {
        EmployeeRole role1 = new EmployeeRole();
        role1.setId(1);
        role1.setRole("Developer");

        EmployeeRole role2 = new EmployeeRole();
        role2.setId(1);
        role2.setRole("Manager");

        assertFalse(role1.equals(role2));
    }

    @Test
    public void testEquals_Null() {
        EmployeeRole role = new EmployeeRole();
        assertFalse(role.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        EmployeeRole role = new EmployeeRole();
        assertFalse(role.equals("not a role"));
    }

    @Test
    public void testToString() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        String result = role.toString();
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("role=Developer"));
    }
}
