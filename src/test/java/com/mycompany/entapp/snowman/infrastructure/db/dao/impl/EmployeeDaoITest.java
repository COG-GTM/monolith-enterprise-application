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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/test-applicationContext.xml"})
@Transactional
public class EmployeeDaoITest {

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

        int employeeId = employee.getId();
        Employee found = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employeeId);

        assertNotNull(found);
        assertEquals("John", found.getFirstname());
        assertEquals("Doe", found.getSurname());
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
        sessionFactory.getCurrentSession().flush();

        int employeeId = employee.getId();
        Employee found = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employeeId);

        assertNotNull(found);
        assertEquals("Jane", found.getFirstname());
        assertEquals("Smith", found.getSurname());
        assertNotNull(found.getRole());
        assertEquals("Developer", found.getRole().getRole());
    }

    @Test
    public void testRetrieveEmployeeReturnsNull() {
        // retrieveEmployee is currently a stub that returns null
        Employee result = employeeDao.retrieveEmployee(999);
        assertNull(result);
    }

    @Test
    public void testUpdateEmployee() {
        Employee employee = new Employee();
        employee.setFirstname("Original");
        employee.setSurname("Name");

        employeeDao.saveEmployee(employee);
        sessionFactory.getCurrentSession().flush();

        int employeeId = employee.getId();

        Employee toUpdate = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employeeId);
        toUpdate.setFirstname("Updated");
        sessionFactory.getCurrentSession().update(toUpdate);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Employee updated = (Employee) sessionFactory.getCurrentSession().get(Employee.class, employeeId);
        assertNotNull(updated);
        assertEquals("Updated", updated.getFirstname());
    }

    @Test
    public void testSaveMultipleEmployees() {
        Employee emp1 = new Employee();
        emp1.setFirstname("Alice");
        emp1.setSurname("Jones");
        employeeDao.saveEmployee(emp1);

        Employee emp2 = new Employee();
        emp2.setFirstname("Bob");
        emp2.setSurname("Brown");
        employeeDao.saveEmployee(emp2);

        sessionFactory.getCurrentSession().flush();

        Employee found1 = (Employee) sessionFactory.getCurrentSession().get(Employee.class, emp1.getId());
        Employee found2 = (Employee) sessionFactory.getCurrentSession().get(Employee.class, emp2.getId());

        assertNotNull(found1);
        assertNotNull(found2);
        assertEquals("Alice", found1.getFirstname());
        assertEquals("Bob", found2.getFirstname());
    }
}
