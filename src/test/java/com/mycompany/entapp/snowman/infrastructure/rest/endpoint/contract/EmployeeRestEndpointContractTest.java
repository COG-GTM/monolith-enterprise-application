package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.contract;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.EmployeeRestEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Employee REST endpoint.
 * Validates that the JSON response structure conforms to the expected API contract.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmployeeRestEndpointContractTest {

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
    public void testGetEmployee_ResponseContainsRequiredFields() throws Exception {
        Employee employee = createTestEmployee();

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.secondName").exists())
                .andExpect(jsonPath("$.role").exists());
    }

    @Test
    public void testGetEmployee_ResponseFieldTypes() throws Exception {
        Employee employee = createTestEmployee();

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").isNumber())
                .andExpect(jsonPath("$.firstName").isString())
                .andExpect(jsonPath("$.secondName").isString())
                .andExpect(jsonPath("$.role").isString());
    }

    @Test
    public void testGetEmployee_ResponseHasNoUnexpectedFields() throws Exception {
        Employee employee = createTestEmployee();

        when(employeeService.getEmployee(1)).thenReturn(employee);

        MvcResult result = mockMvc.perform(get("/employee/{employeeId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertTrue("Response should contain employeeId", json.contains("\"employeeId\""));
        assertTrue("Response should contain firstName", json.contains("\"firstName\""));
        assertTrue("Response should contain secondName", json.contains("\"secondName\""));
        assertTrue("Response should contain role", json.contains("\"role\""));
        assertFalse("Response should not expose internal firstname field", json.contains("\"firstname\""));
        assertFalse("Response should not expose internal surname field", json.contains("\"surname\""));
    }

    @Test
    public void testGetEmployee_ContentTypeIsJson() throws Exception {
        Employee employee = createTestEmployee();

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void testGetEmployee_FieldValuesMappedCorrectly() throws Exception {
        Employee employee = createTestEmployee();

        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employee/{employeeId}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"))
                .andExpect(jsonPath("$.role").value("Developer"));
    }

    private Employee createTestEmployee() {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("John");
        employee.setSurname("Doe");
        employee.setRole(role);
        return employee;
    }
}
