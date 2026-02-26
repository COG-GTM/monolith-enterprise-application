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
@ContextConfiguration(locations = {"classpath:spring/test-application-context.xml"})
@Transactional
public class UserDaoImplITest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS user ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "firstname VARCHAR(50), "
            + "lastname VARCHAR(50), "
            + "username VARCHAR(50), "
            + "password VARCHAR(50), "
            + "email VARCHAR(100))");
    }

    @Test
    public void testSaveAndFindUser() {
        jdbcTemplate.update("INSERT INTO user (firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?)",
            "John", "Doe", "jdoe", "secret", "jdoe@test.com");

        int userId = jdbcTemplate.queryForObject("SELECT id FROM user WHERE username = ?",
            new Object[]{"jdoe"}, Integer.class);

        User found = userDao.findUser(userId);

        assertNotNull(found);
        assertEquals("John", found.getFirstname());
        assertEquals("Doe", found.getLastname());
        assertEquals("jdoe", found.getUsername());
        assertEquals("secret", found.getPassword());
        assertEquals("jdoe@test.com", found.getEmail());
    }

    @Test
    public void testDeleteUser() {
        jdbcTemplate.update("INSERT INTO user (firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?)",
            "Delete", "Me", "deleteme", "pass", "del@test.com");

        int userId = jdbcTemplate.queryForObject("SELECT id FROM user WHERE username = ?",
            new Object[]{"deleteme"}, Integer.class);

        User found = userDao.findUser(userId);
        assertNotNull(found);

        userDao.removeUser(userId);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE id = ?",
            new Object[]{userId}, Integer.class);
        assertEquals(Integer.valueOf(0), count);
    }

    @Test
    public void testFindUserFields() {
        jdbcTemplate.update("INSERT INTO user (firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?)",
            "Alice", "Wonder", "awonder", "pw123", "alice@test.com");

        int userId = jdbcTemplate.queryForObject("SELECT id FROM user WHERE username = ?",
            new Object[]{"awonder"}, Integer.class);

        User found = userDao.findUser(userId);

        assertNotNull(found);
        assertEquals(userId, found.getUserId());
        assertEquals("Alice", found.getFirstname());
        assertEquals("Wonder", found.getLastname());
        assertEquals("awonder", found.getUsername());
        assertEquals("pw123", found.getPassword());
        assertEquals("alice@test.com", found.getEmail());
    }
}
