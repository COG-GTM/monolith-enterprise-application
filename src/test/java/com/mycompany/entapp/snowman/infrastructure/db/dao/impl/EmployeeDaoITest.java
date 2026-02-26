/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
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

public class EmployeeDaoITest extends BaseDaoITest {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveEmployee() {
        Employee employee = createEmployee("John", "Doe");

        employeeDao.saveEmployee(employee);
        sessionFactory.getCurrentSession().flush();

        int generatedId = employee.getId();
        Employee retrieved = (Employee) sessionFactory.getCurrentSession().get(Employee.class, generatedId);
        assertNotNull(retrieved);
        assertEquals("John", retrieved.getFirstname());
        assertEquals("Doe", retrieved.getSurname());
    }

    @Test
    public void testSaveEmployeeWithRole() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");
        sessionFactory.getCurrentSession().save(role);
        sessionFactory.getCurrentSession().flush();

        Employee employee = createEmployee("Jane", "Smith");
        employee.setRole(role);

        employeeDao.saveEmployee(employee);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Employee retrieved = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employee.getId());
        assertNotNull(retrieved);
        assertEquals("Jane", retrieved.getFirstname());
        assertEquals("Smith", retrieved.getSurname());
        assertNotNull(retrieved.getRole());
        assertEquals("Developer", retrieved.getRole().getRole());
    }

    @Test
    public void testRetrieveEmployeeReturnsNull() {
        // The current implementation of retrieveEmployee always returns null
        Employee result = employeeDao.retrieveEmployee(1);
        assertNull(result);
    }

    @Test
    public void testSaveMultipleEmployees() {
        Employee emp1 = createEmployee("Alice", "Brown");
        Employee emp2 = createEmployee("Bob", "White");

        employeeDao.saveEmployee(emp1);
        employeeDao.saveEmployee(emp2);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Employee retrieved1 = (Employee) sessionFactory.getCurrentSession().get(Employee.class, emp1.getId());
        Employee retrieved2 = (Employee) sessionFactory.getCurrentSession().get(Employee.class, emp2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Alice", retrieved1.getFirstname());
        assertEquals("Bob", retrieved2.getFirstname());
    }

    private Employee createEmployee(String firstname, String surname) {
        Employee employee = new Employee();
        employee.setFirstname(firstname);
        employee.setSurname(surname);
        return employee;
    }
}
