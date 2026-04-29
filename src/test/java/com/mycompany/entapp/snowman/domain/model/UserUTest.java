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

public class UserUTest {

    @Test
    public void gettersAndSetters_storeValues() {
        User user = new User();
        user.setUserId(99);
        user.setUsername("alice");
        user.setPassword("secret");
        user.setEmail("alice@example.com");
        user.setFirstname("Alice");
        user.setLastname("Smith");

        assertEquals(99, user.getUserId());
        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("Alice", user.getFirstname());
        assertEquals("Smith", user.getLastname());
    }

    @Test
    public void equalsAndHashCode_followContract() {
        User a = new User();
        a.setUserId(1);
        a.setUsername("alice");

        User b = new User();
        b.setUserId(1);
        b.setUsername("alice");

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));

        b.setUsername("bob");
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("alice");
        assertNotNull(user.toString());
    }
}
