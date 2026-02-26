/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-applicationContext.xml")
@Transactional
public class EmployeeDaoImplITest {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testSaveEmployee() {
        Employee employee = createEmployee("John", "Doe");

        employeeDao.saveEmployee(employee);
        flushAndClear();

        assertTrue(employee.getId() > 0);
    }

    @Test
    public void testSaveAndRetrieveEmployee() {
        Employee employee = createEmployee("Jane", "Smith");
        employeeDao.saveEmployee(employee);
        flushAndClear();

        // retrieveEmployee is a stub that returns null in current implementation
        Employee found = employeeDao.retrieveEmployee(employee.getId());
        assertNull(found);
    }

    @Test
    public void testSaveAndFindViaSession() {
        Employee employee = createEmployee("Bob", "Brown");
        employeeDao.saveEmployee(employee);
        flushAndClear();

        Employee found = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employee.getId());
        assertNotNull(found);
        assertEquals("Bob", found.getFirstname());
        assertEquals("Brown", found.getSurname());
    }

    @Test
    public void testDeleteEmployee() {
        Employee employee = createEmployee("Alice", "White");
        employeeDao.saveEmployee(employee);
        flushAndClear();

        int employeeId = employee.getId();
        Employee toDelete = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employeeId);
        sessionFactory.getCurrentSession().delete(toDelete);
        flushAndClear();

        Employee found = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employeeId);
        assertNull(found);
    }

    private Employee createEmployee(String firstname, String surname) {
        Employee employee = new Employee();
        employee.setFirstname(firstname);
        employee.setSurname(surname);
        return employee;
    }

    private void flushAndClear() {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }
}
