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
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ProjectDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anySetOf;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProjectDTOConverter.class, EmployeeDTOConverter.class})
public class EmployeeDTOConverterUTest {

    @Test
    public void testConvertToEmployeeDTO() {
        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");

        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");
        employee.setRole(role);

        Project project = new Project();
        project.setId(100);
        project.setProjectTitle("Test Project");

        EmployeeProject employeeProject = new EmployeeProject();
        employeeProject.setProject(project);
        employeeProject.setEmployee(employee);

        Set<EmployeeProject> employeeProjects = new HashSet<EmployeeProject>();
        employeeProjects.add(employeeProject);
        employee.setProjects(employeeProjects);

        Set<ProjectDTO> expectedProjectDTOS = new HashSet<ProjectDTO>();
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectId(100);
        projectDTO.setProjectTitle("Test Project");
        expectedProjectDTOS.add(projectDTO);

        PowerMockito.mockStatic(ProjectDTOConverter.class);
        PowerMockito.when(ProjectDTOConverter.convertToProjectDTOS(anySetOf(Project.class))).thenReturn(expectedProjectDTOS);

        EmployeeDTO employeeDTO = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertEquals(1, employeeDTO.getId());
        assertEquals("John", employeeDTO.getFirstName());
        assertEquals("Doe", employeeDTO.getSurname());
        assertEquals("Developer", employeeDTO.getRole());
        assertEquals(1, employeeDTO.getProjectDTOList().size());
        assertEquals(100, employeeDTO.getProjectDTOList().get(0).getProjectId());
    }
}
