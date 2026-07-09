/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.ProjectTestHelper;
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
    public void givenEmployee_whenConvertToEmployeeDTO_thenReturnEmployeeDTO() {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");

        Project project = ProjectTestHelper.getProject();
        EmployeeProject employeeProject = new EmployeeProject();
        employeeProject.setProject(project);

        Set<EmployeeProject> employeeProjects = new HashSet<>();
        employeeProjects.add(employeeProject);

        Employee employee = new Employee();
        employee.setId(3);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);
        employee.setProjects(employeeProjects);

        EmployeeDTO employeeDTO = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertEquals(3, employeeDTO.getId());
        assertEquals("John", employeeDTO.getFirstName());
        assertEquals("Doe", employeeDTO.getSurname());
        assertEquals("Developer", employeeDTO.getRole());
        assertEquals(1, employeeDTO.getProjectDTOList().size());
        assertEquals(project.getProjectTitle(),
            employeeDTO.getProjectDTOList().get(0).getProjectTitle());
    }

    @Test
    public void givenEmployeeWithoutProjects_whenConvertToEmployeeDTO_thenReturnEmptyProjectList() {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Manager");

        Employee employee = new Employee();
        employee.setId(4);
        employee.setFirstname("Jane");
        employee.setSurname("Roe");
        employee.setRole(role);
        employee.setProjects(new HashSet<EmployeeProject>());

        EmployeeDTO employeeDTO = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertEquals(4, employeeDTO.getId());
        assertEquals("Manager", employeeDTO.getRole());
        assertTrue(employeeDTO.getProjectDTOList().isEmpty());
    }
}
