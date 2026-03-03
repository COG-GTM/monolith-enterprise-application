/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeProject;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.EmployeeDTO;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class EmployeeDTOConverterUTest {

    @Test
    public void testConvertToEmployeeDTOWithNoProjects() {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);
        employee.setProjects(new HashSet<EmployeeProject>());

        EmployeeDTO result = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getSurname());
        assertEquals("Developer", result.getRole());
        assertNotNull(result.getProjectDTOList());
        assertTrue(result.getProjectDTOList().isEmpty());
    }

    @Test
    public void testConvertToEmployeeDTOWithProjects() {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Manager");

        Project project = new Project();
        project.setId(10);
        project.setProjectTitle("Big Project");

        Employee employee = new Employee();
        employee.setId(2);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        employee.setRole(role);

        EmployeeProject employeeProject = new EmployeeProject();
        employeeProject.setEmployee(employee);
        employeeProject.setProject(project);

        Set<EmployeeProject> employeeProjects = new HashSet<>();
        employeeProjects.add(employeeProject);
        employee.setProjects(employeeProjects);

        EmployeeDTO result = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getSurname());
        assertEquals("Manager", result.getRole());
        assertNotNull(result.getProjectDTOList());
        assertEquals(1, result.getProjectDTOList().size());
    }
}
