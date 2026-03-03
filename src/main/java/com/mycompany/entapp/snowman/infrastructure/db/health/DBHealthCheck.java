/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.health;

import com.mycompany.entapp.snowman.infrastructure.db.dao.AbstractJDBCDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DBHealthCheck extends AbstractJDBCDao {

    private static final Logger LOG = LoggerFactory.getLogger(DBHealthCheck.class);

    private static final String SELECT_MIN_1_FROM_APP_INFO = "SELECT min(1) from app_info";

    public boolean getDBStatus() {
        setupDBDriver();

        // Java 8: Try-with-resources for automatic resource management
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_MIN_1_FROM_APP_INFO)) {

            if (rs.first()) {
                return true;
            }

        } catch (SQLException e) {
            LOG.error("{}", e);
        }

        return false;
    }
}
