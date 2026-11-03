/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DBHealthCheckUTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private DBHealthCheck classUnderTest = new DBHealthCheck();

    @Test
    public void testGetDBStatus_shouldUseInjectedDataSource() throws SQLException {
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);

        assertTrue(classUnderTest.getDBStatus());
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).close();
    }

    @Test
    public void testGetDBStatus_shouldReturnFalseWhenConnectionFails() throws SQLException {
        Mockito.when(dataSource.getConnection()).thenThrow(new SQLException("Access denied"));

        assertFalse(classUnderTest.getDBStatus());
    }
}
