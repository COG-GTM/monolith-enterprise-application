/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.infrastructure.db.dao.AbstractJDBCDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Integration test for {@link ApplicationInfoDaoImpl}. The DAO hard-codes a MySQL
 * {@code DriverManager} connection via {@link AbstractJDBCDao}, so the inherited
 * {@code getConnection()} is stubbed to hand back a real in-memory H2 connection,
 * letting the DAO's JDBC code path execute against H2.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationInfoDaoImpl.class, AbstractJDBCDao.class})
public class ApplicationInfoDaoImplITest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        // PowerMock's isolating classloader does not auto-register the H2 driver, so load it explicitly.
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:appinfo_" + DB_COUNTER.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE app_info (id INT PRIMARY KEY, version VARCHAR(20))");
        statement.execute("INSERT INTO app_info (id, version) VALUES (1, '1.0.0')");
        statement.execute("INSERT INTO app_info (id, version) VALUES (2, '2.0.0')");
        statement.close();
    }

    @After
    public void tearDown() throws Exception {
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testLoadApplicationInfosShouldReturnRowsFromDatabase() throws Exception {
        ApplicationInfoDaoImpl systemUnderTest = PowerMockito.spy(new ApplicationInfoDaoImpl());
        PowerMockito.doNothing().when(systemUnderTest, "setupDBDriver");
        PowerMockito.doReturn(connection).when(systemUnderTest, "getConnection");

        List<AppInfo> appInfos = systemUnderTest.loadApplicationInfos();

        assertEquals(2, appInfos.size());
        assertEquals(1, appInfos.get(0).getId());
        assertEquals("1.0.0", appInfos.get(0).getVersion());
        assertEquals(2, appInfos.get(1).getId());
        assertEquals("2.0.0", appInfos.get(1).getVersion());
    }
}
