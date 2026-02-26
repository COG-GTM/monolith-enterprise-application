/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.infrastructure.db.dao.EmployeeDao;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
@Transactional
public class EmployeeDaoImplITest {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveEmployee() {
        Employee employee = new Employee();
        employee.setFirstname("John");
        employee.setSurname("Doe");

        employeeDao.saveEmployee(employee);
        sessionFactory.getCurrentSession().flush();

        // Verify the employee was persisted by loading it directly from the session
        Employee retrieved = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employee.getId());
        assertNotNull(retrieved);
        assertEquals("John", retrieved.getFirstname());
        assertEquals("Doe", retrieved.getSurname());
    }

    @Test
    public void testSaveMultipleEmployees() {
        Employee employee1 = new Employee();
        employee1.setFirstname("Jane");
        employee1.setSurname("Smith");

        Employee employee2 = new Employee();
        employee2.setFirstname("Bob");
        employee2.setSurname("Jones");

        employeeDao.saveEmployee(employee1);
        employeeDao.saveEmployee(employee2);
        sessionFactory.getCurrentSession().flush();

        Employee retrieved1 = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employee1.getId());
        Employee retrieved2 = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employee2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Jane", retrieved1.getFirstname());
        assertEquals("Bob", retrieved2.getFirstname());
    }

    @Test
    public void testRetrieveEmployee_returnsNull() {
        // The current implementation always returns null
        Employee retrieved = employeeDao.retrieveEmployee(9999);
        // EmployeeDaoImpl.retrieveEmployee is a stub that returns null
        assertEquals(null, retrieved);
    }
}
