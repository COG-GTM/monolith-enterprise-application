/*
 * |-------------------------------------------------
 * | Copyright (c) 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for DAO integration tests.
 *
 * Loads the test Spring application context with an H2 in-memory database
 * and wraps each test method in a transaction that is rolled back after execution.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-application-context.xml")
@Transactional
public abstract class BaseDAOITest {

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Flushes pending Hibernate operations to the database and clears the session cache.
     * Call this between save and read operations to ensure data is persisted and
     * subsequent reads go to the database rather than the session cache.
     */
    protected void flushAndClear() {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }
}
