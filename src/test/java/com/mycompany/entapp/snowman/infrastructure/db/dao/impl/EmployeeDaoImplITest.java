/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.infrastructure.db.dao.EmployeeDao;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link EmployeeDaoImpl} using H2 in-memory database.
 */
public class EmployeeDaoImplITest extends BaseDAOITest {

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
        flushAndClear();

        Employee retrieved = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, employee.getId());
        assertNotNull("Saved employee should be retrievable from session", retrieved);
        assertEquals("John", retrieved.getFirstname());
        assertEquals("Doe", retrieved.getSurname());
    }

    @Test
    public void testSaveEmployeeWithRole() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");
        sessionFactory.getCurrentSession().save(role);

        Employee employee = new Employee();
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        employee.setRole(role);

        employeeDao.saveEmployee(employee);
        flushAndClear();

        Employee retrieved = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, employee.getId());
        assertNotNull(retrieved);
        assertEquals("Jane", retrieved.getFirstname());
        assertNotNull("Employee role should be persisted", retrieved.getRole());
        assertEquals("Developer", retrieved.getRole().getRole());
    }

    @Test
    public void testRetrieveEmployee_ReturnsNull() {
        // Note: retrieveEmployee is not implemented in EmployeeDaoImpl and always returns null
        Employee employee = new Employee();
        employee.setFirstname("John");
        employee.setSurname("Doe");

        employeeDao.saveEmployee(employee);
        flushAndClear();

        Employee retrieved = employeeDao.retrieveEmployee(employee.getId());
        assertNull("retrieveEmployee is not implemented and always returns null", retrieved);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteEmployee_ThrowsIllegalArgumentException() {
        // Note: deleteEmployee has a bug - it passes an int to session.delete()
        // instead of an entity object, causing an IllegalArgumentException
        Employee employee = new Employee();
        employee.setFirstname("John");
        employee.setSurname("Doe");

        employeeDao.saveEmployee(employee);
        flushAndClear();

        employeeDao.deleteEmployee(employee.getId());
    }

    @Test
    public void testSaveMultipleEmployees() {
        Employee emp1 = new Employee();
        emp1.setFirstname("Alice");
        emp1.setSurname("Johnson");

        Employee emp2 = new Employee();
        emp2.setFirstname("Bob");
        emp2.setSurname("Williams");

        employeeDao.saveEmployee(emp1);
        employeeDao.saveEmployee(emp2);
        flushAndClear();

        Employee retrieved1 = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, emp1.getId());
        Employee retrieved2 = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, emp2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Alice", retrieved1.getFirstname());
        assertEquals("Bob", retrieved2.getFirstname());
        assertTrue("Employees should have different IDs", emp1.getId() != emp2.getId());
    }
}
