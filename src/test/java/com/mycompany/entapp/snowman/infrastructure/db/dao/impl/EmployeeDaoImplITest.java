/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.infrastructure.db.dao.EmployeeDao;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EmployeeDaoImplITest extends BaseDAOIntegrationTest {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveEmployee() {
        Employee employee = createEmployee("John", "Doe");
        employeeDao.saveEmployee(employee);

        sessionFactory.getCurrentSession().flush();

        Employee retrieved = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, employee.getId());
        assertNotNull(retrieved);
        assertEquals("John", retrieved.getFirstname());
        assertEquals("Doe", retrieved.getSurname());
    }

    @Test
    public void testSaveMultipleEmployees() {
        Employee emp1 = createEmployee("Alice", "Smith");
        Employee emp2 = createEmployee("Bob", "Jones");

        employeeDao.saveEmployee(emp1);
        employeeDao.saveEmployee(emp2);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Employee retrieved1 = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, emp1.getId());
        Employee retrieved2 = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, emp2.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Alice", retrieved1.getFirstname());
        assertEquals("Bob", retrieved2.getFirstname());
    }

    @Test
    public void testRetrieveEmployeeReturnsNull() {
        // EmployeeDaoImpl.retrieveEmployee is not yet implemented (returns null)
        Employee result = employeeDao.retrieveEmployee(1);
        assertNull(result);
    }

    @Test
    public void testSaveEmployeeAndVerifyFields() {
        Employee employee = createEmployee("Jane", "Williams");
        employeeDao.saveEmployee(employee);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Employee retrieved = (Employee) sessionFactory.getCurrentSession()
            .get(Employee.class, employee.getId());
        assertNotNull(retrieved);
        assertEquals("Jane", retrieved.getFirstname());
        assertEquals("Williams", retrieved.getSurname());
    }

    private Employee createEmployee(String firstname, String surname) {
        Employee employee = new Employee();
        employee.setFirstname(firstname);
        employee.setSurname(surname);
        return employee;
    }
}
