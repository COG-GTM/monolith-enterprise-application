/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.health;

import com.mycompany.entapp.snowman.infrastructure.db.dao.AbstractJDBCDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractJDBCDao.class, DriverManager.class})
public class DBHealthCheckUTest {

    private static final String SELECT_MIN_1_FROM_APP_INFO = "SELECT min(1) from app_info";

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    private DBHealthCheck classUnderTest = new DBHealthCheck();

    @Before
    public void setUp() throws SQLException {
        PowerMockito.mockStatic(DriverManager.class);
        PowerMockito.when(DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(connection);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(SELECT_MIN_1_FROM_APP_INFO)).thenReturn(resultSet);
    }

    @Test
    public void givenQueryReturnsARow_whenGetDBStatus_thenReturnTrue() throws SQLException {
        Mockito.when(resultSet.first()).thenReturn(true);

        assertTrue(classUnderTest.getDBStatus());

        Mockito.verify(statement).executeQuery(SELECT_MIN_1_FROM_APP_INFO);
        Mockito.verify(connection).close();
        Mockito.verify(statement).close();
    }

    @Test
    public void givenQueryReturnsNoRows_whenGetDBStatus_thenReturnFalse() throws SQLException {
        Mockito.when(resultSet.first()).thenReturn(false);

        assertFalse(classUnderTest.getDBStatus());

        Mockito.verify(connection).close();
        Mockito.verify(statement).close();
    }

    @Test
    public void givenQueryThrowsSQLException_whenGetDBStatus_thenReturnFalse() throws SQLException {
        Mockito.when(statement.executeQuery(SELECT_MIN_1_FROM_APP_INFO)).thenThrow(new SQLException("db down"));

        assertFalse(classUnderTest.getDBStatus());

        Mockito.verify(connection).close();
        Mockito.verify(statement).close();
    }

    @Test
    public void givenConnectionCloseThrowsSQLException_whenGetDBStatus_thenExceptionIsNotPropagated() throws SQLException {
        Mockito.when(resultSet.first()).thenReturn(true);
        Mockito.doThrow(new SQLException("cannot close connection")).when(connection).close();

        assertTrue(classUnderTest.getDBStatus());

        Mockito.verify(statement, Mockito.never()).close();
    }

    @Test
    public void givenStatementCloseThrowsSQLException_whenGetDBStatus_thenExceptionIsNotPropagated() throws SQLException {
        Mockito.when(resultSet.first()).thenReturn(false);
        Mockito.doThrow(new SQLException("cannot close statement")).when(statement).close();

        assertFalse(classUnderTest.getDBStatus());

        Mockito.verify(connection).close();
    }
}
