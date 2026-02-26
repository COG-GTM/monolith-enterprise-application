/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
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

@SuppressWarnings("deprecation")
public class EmployeeRestEndpointITest {

    private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON_UTF8;

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

    @Test
    public void testGetEmployeeReturnsOkWithJsonBody() throws Exception {
        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("DEVELOPER");
        employee.setRole(role);

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));
    }

    @Test
    public void testGetEmployeeReturnsJsonContentType() throws Exception {
        Employee employee = new Employee();
        employee.setId(5);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        EmployeeRole role = new EmployeeRole();
        role.setId(2);
        role.setRole("MANAGER");
        employee.setRole(role);

        when(employeeService.getEmployee(5)).thenReturn(employee);

        mockMvc.perform(get("/employee/5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void testCreateEmployeeReturnsOk() throws Exception {
        doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "John")
                .param("secondName", "Doe")
                .param("role", "DEVELOPER")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(employeeService).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateExistingEmployeeReturnsOk() throws Exception {
        doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "John")
                .param("secondName", "Doe")
                .param("role", "SENIOR_DEVELOPER")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(employeeService).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteExistingEmployeeReturnsOk() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/employee/1/delete"))
                .andExpect(status().isOk());

        verify(employeeService).deleteEmployee(1);
    }
}
