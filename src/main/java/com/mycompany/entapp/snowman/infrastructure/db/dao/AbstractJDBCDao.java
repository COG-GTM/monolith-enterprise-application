/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractJDBCDao {

    @Autowired
    private DataSource dataSource;

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
