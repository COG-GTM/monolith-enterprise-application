/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.model.UserSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String GET_USER_WITH_USERID_QUERY = "SELECT * FROM user where id = ?";

    private static final String DELETE_USER_WITH_USERID = "DELETE FROM user where id = ?";

    private static final String SEARCH_BASE_QUERY = "SELECT * FROM user";

    private static final String COUNT_BASE_QUERY = "SELECT count(*) FROM user";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int i) throws SQLException {
            User user = new User();
            user.setUserId(rs.getInt("id"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            return user;
        }
    };

    @Override
    public User findUser(int userId) {
        return jdbcTemplate.queryForObject(GET_USER_WITH_USERID_QUERY, new Object[]{userId}, USER_ROW_MAPPER);
    }

    private List<String> buildFilters(UserSearchCriteria criteria) {
        List<String> filters = new ArrayList<String>();
        if (criteria.getUsername() != null && criteria.getUsername().length() > 0) {
            filters.add("username like '%" + criteria.getUsername() + "%'");
        }
        if (criteria.getEmail() != null && criteria.getEmail().length() > 0) {
            filters.add("email like '%" + criteria.getEmail() + "%'");
        }
        if (criteria.getDepartment() != null && criteria.getDepartment().length() > 0) {
            filters.add("department = '" + criteria.getDepartment() + "'");
        }
        return filters;
    }

    private String buildWhereClause(UserSearchCriteria criteria) {
        List<String> filters = buildFilters(criteria);
        if (filters.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE ");
        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            sb.append(filters.get(i));
        }
        return sb.toString();
    }

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
        int offset = criteria.getPage() * criteria.getPageSize();
        String query = SEARCH_BASE_QUERY
                + buildWhereClause(criteria)
                + " ORDER BY " + criteria.getSortColumn() + " " + criteria.getSortDirection()
                + " LIMIT " + criteria.getPageSize()
                + " OFFSET " + offset;
        return jdbcTemplate.query(query, USER_ROW_MAPPER);
    }

    @Override
    public long countUsers(UserSearchCriteria criteria) {
        String query = COUNT_BASE_QUERY + buildWhereClause(criteria);
        return jdbcTemplate.queryForObject(query, Long.class);
    }

    @Override
    public void saveUser(User user) {
        // TODO implement
        throw new RuntimeException("Not Yet Implemented");
    }

    @Override
    public void removeUser(int userId) {
        jdbcTemplate.update(DELETE_USER_WITH_USERID, userId);
    }
}
