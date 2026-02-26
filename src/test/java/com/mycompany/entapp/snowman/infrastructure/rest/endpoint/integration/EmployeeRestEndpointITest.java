/*
 * |-------------------------------------------------
 * | Copyright 2024 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Matchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEndpointTestConfig.class)
public class EmployeeRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeService employeeService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(employeeService);
    }

    @Test
    public void testGetEmployee_ReturnsOkWithJson() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.secondName").value("Smith"))
                .andExpect(jsonPath("$.role").value("Developer"));
    }

    @Test(expected = Exception.class)
    public void testGetEmployee_ServiceReturnsNull_ThrowsNpe() throws Exception {
        Mockito.when(employeeService.getEmployee(anyInt())).thenReturn(null);

        mockMvc.perform(get("/employee/999")
                .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateEmployee_ReturnsOk() throws Exception {
        Mockito.doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Developer"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateEmployee_ReturnsOk() throws Exception {
        Mockito.doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Doe")
                .param("role", "Senior Developer"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteEmployee_ReturnsOk() throws Exception {
        Mockito.doNothing().when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/employee/1/delete"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEmployee_CorrectUrlMapping() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setId(2);
        role.setRole("Manager");

        Employee employee = new Employee();
        employee.setId(42);
        employee.setFirstname("Bob");
        employee.setSurname("Jones");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(42)).thenReturn(employee);

        mockMvc.perform(get("/employee/42"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteEmployee_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/employee/1/delete"))
                .andExpect(status().isMethodNotAllowed());
    }
}
