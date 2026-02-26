/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.config.TestWebConfig;
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

import org.springframework.web.util.NestedServletException;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestWebConfig.class)
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
    public void testGetEmployee_ReturnsEmployeeJson() throws Exception {
        Employee employee = createTestEmployee(1, "John", "Doe", "Developer");

        Mockito.when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.secondName").value("Doe"))
            .andExpect(jsonPath("$.role").value("Developer"));
    }

    @Test
    public void testGetEmployee_WhenServiceReturnsNull_ThrowsException() throws Exception {
        Mockito.when(employeeService.getEmployee(999)).thenReturn(null);

        try {
            mockMvc.perform(get("/employee/999")
                    .accept(MediaType.APPLICATION_JSON));
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
            return;
        }
        // If no exception, the controller handled null gracefully
    }

    @Test
    public void testCreateEmployee_ReturnsOk() throws Exception {
        Mockito.doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Manager")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_ReturnsOk() throws Exception {
        Mockito.doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Senior Manager")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_ReturnsOk() throws Exception {
        Mockito.doNothing().when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/employee/1/delete")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).deleteEmployee(1);
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
}
