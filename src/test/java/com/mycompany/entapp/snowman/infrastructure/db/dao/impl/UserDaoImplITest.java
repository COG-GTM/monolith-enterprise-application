/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
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
@ContextConfiguration(locations = "classpath:test-applicationContext.xml")
@Transactional
public class UserDaoImplITest {

    private static final String CREATE_USER_TABLE =
        "CREATE TABLE IF NOT EXISTS user ("
            + "id INT PRIMARY KEY AUTO_INCREMENT, "
            + "firstname VARCHAR(50), "
            + "lastname VARCHAR(50), "
            + "username VARCHAR(50), "
            + "password VARCHAR(100), "
            + "email VARCHAR(100))";

    private static final String INSERT_USER =
        "INSERT INTO user (id, firstname, lastname, username, password, email) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.execute(CREATE_USER_TABLE);
    }

    @Test
    public void testFindUser() {
        jdbcTemplate.update(INSERT_USER, 1, "John", "Doe", "jdoe", "pass123", "jdoe@test.com");

        User user = userDao.findUser(1);

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
        assertEquals("jdoe", user.getUsername());
        assertEquals("pass123", user.getPassword());
        assertEquals("jdoe@test.com", user.getEmail());
    }

    @Test
    public void testRemoveUser() {
        jdbcTemplate.update(INSERT_USER, 2, "Jane", "Smith", "jsmith", "pass456", "jsmith@test.com");

        userDao.removeUser(2);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user WHERE id = ?", Integer.class, 2);
        assertEquals(Integer.valueOf(0), count);
    }

    @Test(expected = RuntimeException.class)
    public void testSaveUser_notImplemented() {
        User user = new User();
        user.setFirstname("Test");
        user.setLastname("User");
        user.setUsername("tuser");
        user.setPassword("pass");
        user.setEmail("tuser@test.com");

        userDao.saveUser(user);
    }

    @Test
    public void testFindAndUpdateUser() {
        jdbcTemplate.update(INSERT_USER, 3, "Bob", "Brown", "bbrown", "pass789", "bbrown@test.com");

        User user = userDao.findUser(3);
        assertNotNull(user);
        assertEquals("Bob", user.getFirstname());

        jdbcTemplate.update("UPDATE user SET firstname = ? WHERE id = ?", "Robert", 3);

        User updated = userDao.findUser(3);
        assertEquals("Robert", updated.getFirstname());
    }
}
