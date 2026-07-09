/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.health;

import com.mycompany.entapp.snowman.infrastructure.db.dao.AbstractJDBCDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DBHealthCheck.class, AbstractJDBCDao.class})
public class DBHealthCheckUTest {

    @Test
    public void testGetDBStatusShouldReturnTrueWhenResultAvailable() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        Mockito.when(resultSet.first()).thenReturn(true);

        DBHealthCheck systemUnderTest = PowerMockito.spy(new DBHealthCheck());
        PowerMockito.doNothing().when(systemUnderTest, "setupDBDriver");
        PowerMockito.doReturn(connection).when(systemUnderTest, "getConnection");

        boolean status = systemUnderTest.getDBStatus();

        assertTrue(status);
        Mockito.verify(connection, Mockito.times(1)).close();
        Mockito.verify(statement, Mockito.times(1)).close();
    }

    @Test
    public void testGetDBStatusShouldReturnFalseWhenNoResult() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        Mockito.when(resultSet.first()).thenReturn(false);

        DBHealthCheck systemUnderTest = PowerMockito.spy(new DBHealthCheck());
        PowerMockito.doNothing().when(systemUnderTest, "setupDBDriver");
        PowerMockito.doReturn(connection).when(systemUnderTest, "getConnection");

        boolean status = systemUnderTest.getDBStatus();

        assertFalse(status);
    }
}
