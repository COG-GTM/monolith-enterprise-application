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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeRestEndpointITest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeRestEndpoint employeeRestEndpoint;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeRestEndpoint).build();
    }

    private Employee createTestEmployee(int id, String firstName, String surname, String roleName) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstname(firstName);
        employee.setSurname(surname);
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole(roleName);
        employee.setRole(role);
        return employee;
    }

    @Test
    public void testGetEmployee_ReturnsOkWithJson() throws Exception {
        Employee employee = createTestEmployee(1, "John", "Doe", "Developer");

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.secondName").value("Doe"))
            .andExpect(jsonPath("$.role").value("Developer"));

        verify(employeeService).getEmployee(1);
    }

    @Test
    public void testGetEmployee_WithDifferentId() throws Exception {
        Employee employee = createTestEmployee(99, "Jane", "Smith", "Manager");

        when(employeeService.getEmployee(99)).thenReturn(employee);

        mockMvc.perform(get("/employee/99"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.employeeId").value(99))
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.secondName").value("Smith"))
            .andExpect(jsonPath("$.role").value("Manager"));

        verify(employeeService).getEmployee(99);
    }

    @Test
    public void testCreateEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "John")
                .param("secondName", "Doe")
                .param("role", "Developer"))
            .andExpect(status().isOk());

        verify(employeeService).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateExistingEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "Updated")
                .param("secondName", "Employee")
                .param("role", "Senior Developer"))
            .andExpect(status().isOk());

        verify(employeeService).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteExistingEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/employee/1/delete"))
            .andExpect(status().isOk());

        verify(employeeService).deleteEmployee(1);
    }

    @Test
    public void testGetEmployee_VerifiesJsonContentType() throws Exception {
        Employee employee = createTestEmployee(1, "John", "Doe", "Developer");

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testDeleteEmployee_WithWrongHttpMethod() throws Exception {
        mockMvc.perform(post("/employee/1/delete"))
            .andExpect(status().isMethodNotAllowed());
    }
}
