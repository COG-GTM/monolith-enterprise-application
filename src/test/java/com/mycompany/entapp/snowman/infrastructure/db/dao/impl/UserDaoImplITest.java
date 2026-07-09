/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Integration test for {@link UserDaoImpl} exercising the real Spring {@link JdbcTemplate}
 * against an in-memory H2 database.
 */
public class UserDaoImplITest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    private JdbcTemplate jdbcTemplate;
    private UserDaoImpl systemUnderTest;

    @Before
    public void setUp() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        // USER is a reserved keyword in H2; NON_KEYWORDS=USER lets the DAO's unquoted
        // `FROM user` / `DELETE FROM user` statements parse against the in-memory database.
        dataSource.setUrl("jdbc:h2:mem:userdao_" + DB_COUNTER.incrementAndGet()
            + ";DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE user ("
            + "id INT PRIMARY KEY, "
            + "firstname VARCHAR(20), "
            + "lastname VARCHAR(20), "
            + "username VARCHAR(20), "
            + "password VARCHAR(20), "
            + "email VARCHAR(40))");
        jdbcTemplate.update("INSERT INTO user (id, firstname, lastname, username, password, email) "
            + "VALUES (?, ?, ?, ?, ?, ?)",
            1, "John", "Doe", "jdoe", "secret", "jdoe@example.com");

        systemUnderTest = new UserDaoImpl();
        Field jdbcTemplateField = UserDaoImpl.class.getDeclaredField("jdbcTemplate");
        jdbcTemplateField.setAccessible(true);
        jdbcTemplateField.set(systemUnderTest, jdbcTemplate);
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("DROP TABLE user");
    }

    @Test
    public void testFindUserShouldReturnPersistedUser() {
        User user = systemUnderTest.findUser(1);

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
        assertEquals("jdoe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("jdoe@example.com", user.getEmail());
    }

    @Test
    public void testRemoveUserShouldDeleteUser() {
        systemUnderTest.removeUser(1);

        int remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE id = ?",
            Integer.class, 1);
        assertEquals(0, remaining);
    }

    @Test(expected = RuntimeException.class)
    public void testSaveUserShouldThrowNotYetImplemented() {
        systemUnderTest.saveUser(new User());
    }
}
