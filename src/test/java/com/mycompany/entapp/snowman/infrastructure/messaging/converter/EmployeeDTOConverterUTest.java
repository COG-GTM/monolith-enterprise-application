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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EmployeeDTOConverterUTest {

    @Test
    public void convertToEmployeeDTO_copiesFieldsAndProjects() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        Project project = new Project();
        project.setId(7);
        project.setProjectTitle("Phoenix");

        EmployeeProject employeeProject = new EmployeeProject();
        employeeProject.setProject(project);

        Set<EmployeeProject> employeeProjects = new HashSet<EmployeeProject>();
        employeeProjects.add(employeeProject);

        Employee employee = new Employee();
        employee.setId(42);
        employee.setFirstname("Alice");
        employee.setSurname("Smith");
        employee.setRole(role);
        employee.setProjects(employeeProjects);

        EmployeeDTO dto = EmployeeDTOConverter.convertToEmployeeDTO(employee);

        assertNotNull(dto);
        assertEquals(42, dto.getId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getSurname());
        assertEquals("Developer", dto.getRole());
        assertNotNull(dto.getProjectDTOList());
        assertEquals(1, dto.getProjectDTOList().size());
        assertEquals("Phoenix", dto.getProjectDTOList().get(0).getProjectTitle());
    }
}
