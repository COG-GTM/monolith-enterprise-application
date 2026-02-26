/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserDaoITest extends BaseDaoITest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            1, "jdoe", "secret", "jdoe@test.com", "John", "Doe"
        );

        User user = userDao.findUser(1);
        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("jdoe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("jdoe@test.com", user.getEmail());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testFindUserNotFound() {
        userDao.findUser(99999);
    }

    @Test
    public void testRemoveUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            2, "asmith", "pass123", "asmith@test.com", "Alice", "Smith"
        );

        // Verify the user exists
        User user = userDao.findUser(2);
        assertNotNull(user);

        // Remove the user
        userDao.removeUser(2);

        // Verify user count is 0 for that id
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user WHERE id = ?", Integer.class, 2
        );
        assertEquals(Integer.valueOf(0), count);
    }

    @Test
    public void testFindUserFieldMapping() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            3, "testuser", "testpass", "test@example.com", "Test", "User"
        );

        User user = userDao.findUser(3);
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("testpass", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getFirstname());
        assertEquals("User", user.getLastname());
    }

    @Test(expected = RuntimeException.class)
    public void testSaveUserNotImplemented() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("newpass");
        user.setEmail("new@test.com");
        user.setFirstname("New");
        user.setLastname("User");

        userDao.saveUser(user);
    }
}
