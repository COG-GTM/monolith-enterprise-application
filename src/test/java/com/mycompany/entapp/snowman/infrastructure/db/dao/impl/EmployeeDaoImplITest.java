package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.infrastructure.db.dao.EmployeeDao;
import org.hibernate.Session;
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
@ContextConfiguration(locations = {"classpath:spring/test-application-context.xml"})
@Transactional
public class EmployeeDaoImplITest {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    public void testSaveAndFindEmployee() {
        Employee employee = new Employee();
        employee.setFirstname("John");
        employee.setSurname("Doe");

        employeeDao.saveEmployee(employee);
        getCurrentSession().flush();

        int employeeId = employee.getId();
        Employee found = (Employee) getCurrentSession().get(Employee.class, employeeId);

        assertNotNull(found);
        assertEquals("John", found.getFirstname());
        assertEquals("Doe", found.getSurname());
    }

    @Test
    public void testUpdateEmployee() {
        Employee employee = new Employee();
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        employeeDao.saveEmployee(employee);
        getCurrentSession().flush();

        int employeeId = employee.getId();
        Employee found = (Employee) getCurrentSession().get(Employee.class, employeeId);
        found.setFirstname("Janet");
        getCurrentSession().update(found);
        getCurrentSession().flush();

        Employee updated = (Employee) getCurrentSession().get(Employee.class, employeeId);
        assertEquals("Janet", updated.getFirstname());
    }

    @Test
    public void testDeleteEmployee() {
        Employee employee = new Employee();
        employee.setFirstname("ToDelete");
        employee.setSurname("Person");
        employeeDao.saveEmployee(employee);
        getCurrentSession().flush();

        int employeeId = employee.getId();
        assertNotNull(getCurrentSession().get(Employee.class, employeeId));

        Employee toDelete = (Employee) getCurrentSession().get(Employee.class, employeeId);
        getCurrentSession().delete(toDelete);
        getCurrentSession().flush();

        Employee deleted = (Employee) getCurrentSession().get(Employee.class, employeeId);
        assertNull(deleted);
    }

    @Test
    public void testFindNonExistentEmployee() {
        Employee found = (Employee) getCurrentSession().get(Employee.class, 99999);
        assertNull(found);
    }
}
