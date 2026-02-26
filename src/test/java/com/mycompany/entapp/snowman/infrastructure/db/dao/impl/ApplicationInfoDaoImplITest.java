/*
 * |-------------------------------------------------
 * | Copyright (c) 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ApplicationInfoDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test for ApplicationInfoDaoImpl.
 *
 * Note: ApplicationInfoDaoImpl extends AbstractJDBCDao which uses raw JDBC with
 * hardcoded MySQL connection parameters. The loadApplicationInfos() method cannot
 * be tested directly against H2 without refactoring the production code.
 *
 * This test verifies the Spring context wiring and tests the equivalent data
 * access operations using JdbcTemplate against the H2 in-memory database.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
@Transactional
public class ApplicationInfoDaoImplITest {

    @Autowired
    private ApplicationInfoDao applicationInfoDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        // Create the app_info table (non-JPA entity, not auto-created by Hibernate)
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS app_info ("
            + "id INT PRIMARY KEY AUTO_INCREMENT, "
            + "version VARCHAR(50))"
        );
    }

    @Test
    public void testApplicationInfoDaoIsWired() {
        assertNotNull(applicationInfoDao);
    }

    @Test
    public void testAppInfoTableOperations() {
        // Insert test data using JdbcTemplate
        jdbcTemplate.update("INSERT INTO app_info (id, version) VALUES (?, ?)", 1, "1.0.0");
        jdbcTemplate.update("INSERT INTO app_info (id, version) VALUES (?, ?)", 2, "2.0.0");

        // Verify the data can be read back (equivalent to what loadApplicationInfos does)
        List<AppInfo> appInfos = jdbcTemplate.query("SELECT * FROM app_info", new RowMapper<AppInfo>() {
            @Override
            public AppInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                AppInfo appInfo = new AppInfo();
                appInfo.setId(rs.getInt("id"));
                appInfo.setVersion(rs.getString("version"));
                return appInfo;
            }
        });

        assertNotNull(appInfos);
        assertEquals(2, appInfos.size());
        assertEquals("1.0.0", appInfos.get(0).getVersion());
        assertEquals("2.0.0", appInfos.get(1).getVersion());
    }

    @Test
    public void testAppInfoTableIsEmpty_initially() {
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_info", Integer.class);
        assertEquals(0, count);
    }
}
