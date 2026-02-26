/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeRestEndpointITest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeRestEndpoint employeeRestEndpoint;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeRestEndpoint).build();
    }

    @Test
    public void testGetEmployee() throws Exception {
        int employeeId = 1;
        Employee employee = createEmployee(employeeId);

        when(employeeService.getEmployee(employeeId)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", employeeId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.employeeId", is(1)))
            .andExpect(jsonPath("$.firstName", is("Jane")))
            .andExpect(jsonPath("$.secondName", is("Smith")))
            .andExpect(jsonPath("$.role", is("Developer")));

        verify(employeeService, times(1)).getEmployee(employeeId);
    }

    @Test
    public void testCreateEmployee() throws Exception {
        doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Developer"))
            .andExpect(status().isOk());

        verify(employeeService, times(1)).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateExistingEmployee() throws Exception {
        doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Senior Developer"))
            .andExpect(status().isOk());

        verify(employeeService, times(1)).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteExistingEmployee() throws Exception {
        int employeeId = 1;
        doNothing().when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/employee/{employeeId}/delete", employeeId))
            .andExpect(status().isOk());

        verify(employeeService, times(1)).deleteEmployee(employeeId);
    }

    private Employee createEmployee(int employeeId) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");
        employee.setRole(role);
        return employee;
    }
}
