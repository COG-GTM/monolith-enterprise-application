/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import com.mycompany.entapp.snowman.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String GET_USER_WITH_USERID_QUERY = "SELECT * FROM user where id = ?";

    private static final String GET_USER_WITH_USERNAME_QUERY = "SELECT * FROM user where username = ?";

    private static final String INSERT_USER = "INSERT INTO user (id, username, password, email, firstname, secondname) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_USER_WITH_USERID = "DELETE FROM user where id = ?";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int i) throws SQLException {
            User user = new User();
            user.setUserId(rs.getInt("id"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("secondname"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            return user;
        }
    };

    @Override
    public User findUser(int userId) {
        return jdbcTemplate.queryForObject(GET_USER_WITH_USERID_QUERY, new Object[]{userId}, userRowMapper);
    }

    @Override
    public User findByUsername(String username) {
        List<User> users = jdbcTemplate.query(GET_USER_WITH_USERNAME_QUERY, new Object[]{username}, userRowMapper);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public void saveUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        jdbcTemplate.update(INSERT_USER,
            user.getUserId(),
            user.getUsername(),
            hashedPassword,
            user.getEmail(),
            user.getFirstname(),
            user.getLastname());
    }

    @Override
    public void removeUser(int userId) {
        jdbcTemplate.update(DELETE_USER_WITH_USERID, userId);
    }
}
