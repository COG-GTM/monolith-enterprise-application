/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.EmployeeResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.EmployeeResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

        Employee employee = new Employee();
        EmployeeResource employeeResource = new EmployeeResource();

        try (MockedStatic<EmployeeResourceMapper> mockedMapper = Mockito.mockStatic(EmployeeResourceMapper.class)) {
            mockedMapper.when(() -> EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee)).thenReturn(employeeResource);

            ResponseEntity<EmployeeResource> responseEntity = systemUnderTest.getEmployee(employeeId);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(employeeResource, responseEntity.getBody());
        }
    }

    @Test
    public void testCreateEmployeeShouldCreateEmployee() {
        Employee employee = new Employee();
        EmployeeResource employeeResource = new EmployeeResource();

        try (MockedStatic<EmployeeResourceMapper> mockedMapper = Mockito.mockStatic(EmployeeResourceMapper.class)) {
            mockedMapper.when(() -> EmployeeResourceMapper.mapEmployeeResourceToEmployee(employeeResource)).thenReturn(employee);
            Mockito.doNothing().when(employeeService).createEmployee(employee);

            systemUnderTest.createEmployee(employeeResource);

            mockedMapper.verify(() -> EmployeeResourceMapper.mapEmployeeResourceToEmployee(employeeResource));
            Mockito.verify(employeeService, Mockito.times(1)).createEmployee(employee);
        }
    }

    @Test
    public void testUpdateExistingEmployeeShouldUpdateExistingEmployee() {
        Employee employee = new Employee();
        EmployeeResource employeeResource = new EmployeeResource();

        try (MockedStatic<EmployeeResourceMapper> mockedMapper = Mockito.mockStatic(EmployeeResourceMapper.class)) {
            mockedMapper.when(() -> EmployeeResourceMapper.mapEmployeeResourceToEmployee(employeeResource)).thenReturn(employee);
            Mockito.doNothing().when(employeeService).updateEmployee(employee);

            systemUnderTest.updateExistingEmployee(employeeResource);

            mockedMapper.verify(() -> EmployeeResourceMapper.mapEmployeeResourceToEmployee(employeeResource));
            Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(employee);
        }
    }

    @Test
    public void testDeleteEmployeeShouldDeleteEmployee(){
        int employeeId = 7;

        Mockito.doNothing().when(employeeService).deleteEmployee(employeeId);

        systemUnderTest.deleteExistingEmployee(employeeId);

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }

}
