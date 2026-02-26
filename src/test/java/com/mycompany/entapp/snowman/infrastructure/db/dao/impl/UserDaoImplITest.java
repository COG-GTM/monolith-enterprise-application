/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserDaoImplITest extends BaseDAOIntegrationTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            1, "jdoe", "pass123", "jdoe@test.com", "John", "Doe"
        );

        User user = userDao.findUser(1);

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("jdoe", user.getUsername());
        assertEquals("pass123", user.getPassword());
        assertEquals("jdoe@test.com", user.getEmail());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
    }

    @Test
    public void testRemoveUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            2, "asmith", "pass456", "asmith@test.com", "Alice", "Smith"
        );

        userDao.removeUser(2);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user WHERE id = ?",
            Integer.class,
            2
        );
        assertEquals(Integer.valueOf(0), count);
    }

    @Test
    public void testFindUserWithAllFields() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            3, "bwilliams", "secret", "bwilliams@test.com", "Bob", "Williams"
        );

        User user = userDao.findUser(3);

        assertNotNull(user);
        assertEquals("bwilliams", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("bwilliams@test.com", user.getEmail());
        assertEquals("Bob", user.getFirstname());
        assertEquals("Williams", user.getLastname());
    }

    @Test(expected = RuntimeException.class)
    public void testSaveUserNotYetImplemented() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass");
        user.setEmail("test@test.com");
        user.setFirstname("Test");
        user.setLastname("User");

        // saveUser is not yet implemented and throws RuntimeException
        userDao.saveUser(user);
    }
}
