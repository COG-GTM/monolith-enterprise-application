/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.infrastructure.db.dao.HibernateH2TestSupport;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration test for {@link EmployeeDaoImpl} exercising real Hibernate persistence
 * against an in-memory H2 database.
 */
public class EmployeeDaoImplITest {

    private SessionFactory sessionFactory;
    private EmployeeDaoImpl systemUnderTest;

    @Before
    public void setUp() {
        sessionFactory = HibernateH2TestSupport.buildSessionFactory();
        systemUnderTest = new EmployeeDaoImpl();
        HibernateH2TestSupport.injectSessionFactory(systemUnderTest, sessionFactory);
    }

    @After
    public void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void testSaveEmployeeShouldPersistEmployee() {
        Employee employee = new Employee();
        employee.setFirstname("John");
        employee.setSurname("Doe");

        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        systemUnderTest.saveEmployee(employee);
        tx.commit();

        int generatedId = employee.getId();

        Transaction readTx = sessionFactory.getCurrentSession().beginTransaction();
        Employee persisted = (Employee) sessionFactory.getCurrentSession().get(Employee.class, generatedId);
        readTx.commit();

        assertNotNull(persisted);
        assertEquals("John", persisted.getFirstname());
        assertEquals("Doe", persisted.getSurname());
    }

    @Test
    public void testRetrieveEmployeeReturnsCurrentImplementationResult() {
        // retrieveEmployee is currently an unimplemented stub that always returns null.
        Employee employee = systemUnderTest.retrieveEmployee(1);

        assertNull(employee);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteEmployeeDelegatesDeleteToSession() {
        Employee employee = new Employee();
        employee.setFirstname("Jane");
        employee.setSurname("Roe");

        Transaction saveTx = sessionFactory.getCurrentSession().beginTransaction();
        systemUnderTest.saveEmployee(employee);
        saveTx.commit();

        // deleteEmployee(int) delegates to Session.delete(Object) with an auto-boxed Integer,
        // which Hibernate rejects because Integer is not a mapped entity.
        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        try {
            systemUnderTest.deleteEmployee(employee.getId());
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }
}
