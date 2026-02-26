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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
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
    public void testGetEmployee_shouldReturnOkWithEmployeeResource() throws Exception {
        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");
        employee.setRole(role);

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.secondName").value("Smith"))
                .andExpect(jsonPath("$.role").value("Developer"));
    }

    @Test
    public void testCreateEmployee_shouldReturnOk() throws Exception {
        doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Developer"))
                .andExpect(status().isOk());

        verify(employeeService).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_shouldReturnOk() throws Exception {
        doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Developer"))
                .andExpect(status().isOk());

        verify(employeeService).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_shouldReturnOk() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/employee/1/delete"))
                .andExpect(status().isOk());

        verify(employeeService).deleteEmployee(1);
    }
}
