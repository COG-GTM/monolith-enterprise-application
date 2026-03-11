/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.EmployeeResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeRestEndpointUTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeRestEndpoint systemUnderTest = new EmployeeRestEndpoint();

    @Test
    public void testGetEmployeeShouldGetEmployee() {
        Integer employeeId = 5;

        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(employeeId)).thenReturn(employee);

        ResponseEntity<EmployeeResource> responseEntity = systemUnderTest.getEmployee(employeeId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testCreateEmployeeShouldCreateEmployee() {
        EmployeeResource employeeResource = new EmployeeResource();
        employeeResource.setEmployeeId(1);
        employeeResource.setFirstName("John");
        employeeResource.setSecondName("Doe");
        employeeResource.setRole("Developer");

        systemUnderTest.createEmployee(employeeResource);

        Mockito.verify(employeeService, Mockito.times(1)).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateExistingEmployeeShouldUpdateExistingEmployee() {
        EmployeeResource employeeResource = new EmployeeResource();
        employeeResource.setEmployeeId(1);
        employeeResource.setFirstName("John");
        employeeResource.setSecondName("Doe");
        employeeResource.setRole("Developer");

        systemUnderTest.updateExistingEmployee(employeeResource);

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteEmployeeShouldDeleteEmployee(){
        int employeeId = 7;

        Mockito.doNothing().when(employeeService).deleteEmployee(employeeId);

        systemUnderTest.deleteExistingEmployee(employeeId);

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }

}
