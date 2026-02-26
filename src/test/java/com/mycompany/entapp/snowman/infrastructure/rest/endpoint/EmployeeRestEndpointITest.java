/*
 * Integration tests for EmployeeRestEndpoint using MockMvc.
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.config.RestIntegrationTestConfig;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestIntegrationTestConfig.class)
@WebAppConfiguration
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
    public void testGetEmployee_returnsOkWithEmployeeJson() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.secondName").value("Doe"))
            .andExpect(jsonPath("$.role").value("Developer"));
    }

    @Test
    public void testGetEmployee_verifyServiceCalled() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Tester");

        Employee employee = new Employee();
        employee.setId(42);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(42)).thenReturn(employee);

        mockMvc.perform(get("/employee/42").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).getEmployee(42);
    }

    @Test
    public void testCreateEmployee_returnsOk() throws Exception {
        mockMvc.perform(post("/employee/create")
                .param("employeeId", "1")
                .param("firstName", "New")
                .param("secondName", "Employee")
                .param("role", "Manager"))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).createEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_returnsOk() throws Exception {
        mockMvc.perform(post("/employee/update")
                .param("employeeId", "1")
                .param("firstName", "Updated")
                .param("secondName", "Employee")
                .param("role", "Senior"))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).updateEmployee(Mockito.any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_returnsOk() throws Exception {
        mockMvc.perform(delete("/employee/1/delete"))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).deleteEmployee(1);
    }

    @Test
    public void testGetEmployee_returnsJsonContentType() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setRole("Analyst");

        Employee employee = new Employee();
        employee.setId(5);
        employee.setFirstname("Content");
        employee.setSurname("Type");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(5)).thenReturn(employee);

        mockMvc.perform(get("/employee/5").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetEmployee_invalidMethod_returns405() throws Exception {
        mockMvc.perform(post("/employee/1"))
            .andExpect(status().isMethodNotAllowed());
    }
}
