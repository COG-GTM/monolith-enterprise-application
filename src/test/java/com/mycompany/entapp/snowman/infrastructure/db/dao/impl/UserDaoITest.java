/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
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
@ContextConfiguration(locations = {"classpath:spring/test-applicationContext.xml"})
@Transactional
public class UserDaoITest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.update("DELETE FROM user");
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            1, "jdoe", "secret", "jdoe@example.com", "John", "Doe"
        );
    }

    @Test
    public void testFindUser() {
        User user = userDao.findUser(1);

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("jdoe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("jdoe@example.com", user.getEmail());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
    }

    @Test
    public void testRemoveUser() {
        userDao.removeUser(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE id = ?", Integer.class, 1);
        assertEquals(Integer.valueOf(0), count);
    }

    @Test
    public void testFindUserAfterInsert() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            2, "asmith", "pass123", "asmith@example.com", "Alice", "Smith"
        );

        User user = userDao.findUser(2);

        assertNotNull(user);
        assertEquals(2, user.getUserId());
        assertEquals("asmith", user.getUsername());
        assertEquals("Alice", user.getFirstname());
        assertEquals("Smith", user.getLastname());
    }

    @Test
    public void testRemoveUserAndVerifyOthersRemain() {
        jdbcTemplate.update(
            "INSERT INTO user (id, username, password, email, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)",
            3, "bwilson", "pw", "bwilson@example.com", "Bob", "Wilson"
        );

        userDao.removeUser(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
        assertEquals(Integer.valueOf(1), count);

        User remaining = userDao.findUser(3);
        assertNotNull(remaining);
        assertEquals("bwilson", remaining.getUsername());
    }
}
