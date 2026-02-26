/*
 * |-------------------------------------------------
 * | Copyright (c) 2017 Colin But. All rights reserved.
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

/**
 * Integration tests for {@link UserDaoImpl} using H2 in-memory database.
 *
 * Note: UserDaoImpl uses JdbcTemplate (not Hibernate) for database operations.
 * The User table is created via test-schema.sql since the User model has no JPA annotations.
 */
public class UserDaoImplITest extends BaseDAOITest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            1, "jdoe", "secret", "jdoe@example.com", "John", "Doe"
        );

        User user = userDao.findUser(1);

        assertNotNull("User should be found", user);
        assertEquals(1, user.getUserId());
        assertEquals("jdoe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("jdoe@example.com", user.getEmail());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testFindUser_NotFound() {
        userDao.findUser(999);
    }

    @Test
    public void testRemoveUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            1, "jdoe", "secret", "jdoe@example.com", "John", "Doe"
        );

        userDao.removeUser(1);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user WHERE id = ?", Integer.class, 1
        );
        assertEquals("User should be removed", Integer.valueOf(0), count);
    }

    @Test(expected = RuntimeException.class)
    public void testSaveUser_NotImplemented() {
        // Note: saveUser is not yet implemented and throws RuntimeException
        User user = new User();
        user.setUserId(1);
        user.setUsername("jdoe");
        user.setPassword("secret");
        user.setEmail("jdoe@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        userDao.saveUser(user);
    }

    @Test
    public void testRemoveUser_NoRowsAffected() {
        // Removing a non-existent user should not throw an exception
        userDao.removeUser(999);
    }

    @Test
    public void testFindUser_AllFieldsMapped() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            42, "asmith", "pass123", "asmith@test.com", "Alice", "Smith"
        );

        User user = userDao.findUser(42);

        assertNotNull(user);
        assertEquals(42, user.getUserId());
        assertEquals("asmith", user.getUsername());
        assertEquals("pass123", user.getPassword());
        assertEquals("asmith@test.com", user.getEmail());
        assertEquals("Alice", user.getFirstname());
        assertEquals("Smith", user.getLastname());
    }
}
