/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.repository.impl;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.repository.EmployeeRepository;
import com.mycompany.entapp.snowman.infrastructure.db.dao.EmployeeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {

    @Autowired
    private EmployeeDao employeeDao;

    @Transactional(readOnly = true)
    @Override
    public Employee findEmployee(int employeeId) {
        return employeeDao.retrieveEmployee(employeeId);
    }

    @Transactional
    @Override
    public void saveEmployee(Employee employee) {
        employeeDao.saveEmployee(employee);
    }

    @Transactional
    @Override
    public void removeEmployee(int employeeId) {
        employeeDao.deleteEmployee(employeeId);
    }
}
