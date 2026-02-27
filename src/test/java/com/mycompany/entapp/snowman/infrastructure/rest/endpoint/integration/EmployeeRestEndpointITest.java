package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.EmployeeRestEndpoint;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void testGetEmployee_ReturnsOkWithEmployeeData() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(5);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);

        when(employeeService.getEmployee(5)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", 5)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"))
                .andExpect(jsonPath("$.role").value("Developer"));

        verify(employeeService).getEmployee(5);
    }

    @Test
    public void testCreateEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).createEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/create")
                .param("employeeId", "0")
                .param("firstName", "Jane")
                .param("secondName", "Smith")
                .param("role", "Manager"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateExistingEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).updateEmployee(Mockito.any(Employee.class));

        mockMvc.perform(post("/employee/update")
                .param("employeeId", "5")
                .param("firstName", "John")
                .param("secondName", "Updated")
                .param("role", "Senior Developer"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteExistingEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).deleteEmployee(7);

        mockMvc.perform(delete("/employee/{employeeId}/delete", 7))
                .andExpect(status().isOk());

        verify(employeeService).deleteEmployee(7);
    }

    @Test
    public void testGetEmployee_WithInvalidId_Returns4xx() throws Exception {
        mockMvc.perform(get("/employee/{employeeId}", "invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetEmployee_VerifiesServiceInteraction() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Tester");

        Employee employee = new Employee();
        employee.setId(10);
        employee.setFirstname("Alice");
        employee.setSurname("Wonder");
        employee.setRole(role);

        when(employeeService.getEmployee(10)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", 10)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService).getEmployee(10);
        Mockito.verifyNoMoreInteractions(employeeService);
    }
}
