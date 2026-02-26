/*
 * |-------------------------------------------------
 * | Copyright (c) 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
@Transactional
public class UserDaoImplITest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        // Create the user table if it does not exist (non-JPA entity)
        // H2 URL uses NON_KEYWORDS=USER so "user" is not reserved
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS user ("
            + "id INT PRIMARY KEY AUTO_INCREMENT, "
            + "firstname VARCHAR(50), "
            + "lastname VARCHAR(50), "
            + "username VARCHAR(50), "
            + "password VARCHAR(100), "
            + "email VARCHAR(100))"
        );
    }

    @Test
    public void testFindUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?, ?)",
            1, "John", "Doe", "johndoe", "secret", "john@example.com"
        );

        User user = userDao.findUser(1);

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
        assertEquals("johndoe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    public void testRemoveUser() {
        jdbcTemplate.update(
            "INSERT INTO user (id, firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?, ?)",
            2, "Jane", "Smith", "janesmith", "pass123", "jane@example.com"
        );

        // Verify user exists
        User user = userDao.findUser(2);
        assertNotNull(user);

        // Remove the user
        userDao.removeUser(2);

        // Verify user was removed by checking row count
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE id = ?", Integer.class, 2);
        assertEquals(0, count);
    }

    @Test
    public void testFindUser_multipleUsers() {
        jdbcTemplate.update(
            "INSERT INTO user (id, firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?, ?)",
            10, "Alice", "Wonder", "alice", "pw1", "alice@example.com"
        );
        jdbcTemplate.update(
            "INSERT INTO user (id, firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?, ?)",
            11, "Bob", "Builder", "bob", "pw2", "bob@example.com"
        );

        User alice = userDao.findUser(10);
        User bob = userDao.findUser(11);

        assertNotNull(alice);
        assertEquals("Alice", alice.getFirstname());

        assertNotNull(bob);
        assertEquals("Bob", bob.getFirstname());
    }
}
