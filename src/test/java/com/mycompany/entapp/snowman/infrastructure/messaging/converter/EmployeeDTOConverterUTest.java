/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.EmployeeTestHelper;
import com.mycompany.entapp.snowman.domain.ProjectTestHelper;
import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeProject;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.EmployeeDTO;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmployeeDTOConverterUTest {

    @Test
    public void testConvertToEmployeeDTO() {
        Project project = ProjectTestHelper.getProject();

        EmployeeProject employeeProject = new EmployeeProject();
        employeeProject.setProject(project);

        Set<EmployeeProject> employeeProjects = new HashSet<>();
        employeeProjects.add(employeeProject);

        Employee employee = EmployeeTestHelper.getEmployee();
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setRole("Developer");
        employee.setRole(employeeRole);
        employee.setProjects(employeeProjects);

        EmployeeDTO employeeDTO = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertEquals(employee.getId(), employeeDTO.getId());
        assertEquals(employee.getFirstname(), employeeDTO.getFirstName());
        assertEquals(employee.getSurname(), employeeDTO.getSurname());
        assertEquals("Developer", employeeDTO.getRole());
        assertEquals(1, employeeDTO.getProjectDTOList().size());
        assertEquals(project.getId(), employeeDTO.getProjectDTOList().get(0).getProjectId());
    }

    @Test
    public void testConvertToEmployeeDTOGivenEmployeeWithNoProjects() {
        Employee employee = EmployeeTestHelper.getEmployee();
        employee.setProjects(new HashSet<EmployeeProject>());

        EmployeeDTO employeeDTO = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertTrue(employeeDTO.getProjectDTOList().isEmpty());
    }
}
