/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EmployeeRoleUTest {

    @Test
    public void gettersAndSetters_storeValues() {
        EmployeeRole role = new EmployeeRole();
        role.setId(2);
        role.setRole("Developer");

        assertEquals(2, role.getId());
        assertEquals("Developer", role.getRole());
    }

    @Test
    public void equalsAndHashCode_followContract() {
        EmployeeRole a = new EmployeeRole();
        a.setId(1);
        a.setRole("Manager");

        EmployeeRole b = new EmployeeRole();
        b.setId(1);
        b.setRole("Manager");

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));

        b.setRole("Developer");
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Manager");
        assertNotNull(role.toString());
    }
}
