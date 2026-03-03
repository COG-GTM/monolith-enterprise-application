/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserUTest {

    @Test
    public void testGettersAndSetters() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        assertEquals(1, user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");

        assertTrue(user.equals(user));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        User user1 = new User();
        user1.setUserId(1);
        user1.setUsername("testuser");
        user1.setPassword("pass");
        user1.setEmail("test@example.com");
        user1.setFirstname("John");
        user1.setLastname("Doe");

        User user2 = new User();
        user2.setUserId(1);
        user2.setUsername("testuser");
        user2.setPassword("pass");
        user2.setEmail("test@example.com");
        user2.setFirstname("John");
        user2.setLastname("Doe");

        assertTrue(user1.equals(user2));
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentId() {
        User user1 = new User();
        user1.setUserId(1);
        user1.setUsername("testuser");

        User user2 = new User();
        user2.setUserId(2);
        user2.setUsername("testuser");

        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEqualsAndHashCode_DifferentUsername() {
        User user1 = new User();
        user1.setUserId(1);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUserId(1);
        user2.setUsername("user2");

        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_Null() {
        User user = new User();
        assertFalse(user.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        User user = new User();
        assertFalse(user.equals("not a user"));
    }

    @Test
    public void testToString() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        String result = user.toString();
        assertNotNull(result);
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("username=testuser"));
        assertTrue(result.contains("email=test@example.com"));
    }
}
