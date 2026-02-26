/*
 * |-------------------------------------------------
 * | Copyright 2024 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import org.joda.time.DateTime;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests that verify the API request/response shapes (field names, types, structure)
 * for all REST endpoints.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEndpointTestConfig.class)
public class RestEndpointContractITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ApplicationInfoService applicationInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectService projectService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        Mockito.reset(applicationInfoService, userService, employeeService, clientService, projectService);
    }

    // ========== AppInfo Contract ==========

    @Test
    public void testAppInfoContract_ResponseContainsExpectedFields() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");

        Mockito.when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        MvcResult result = mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);

        assertTrue("Response should contain 'id' field", responseMap.containsKey("id"));
        assertTrue("Response should contain 'version' field", responseMap.containsKey("version"));
        assertEquals("Field count should be 2", 2, responseMap.size());

        assertTrue("'id' should be a number", responseMap.get("id") instanceof Number);
        assertTrue("'version' should be a string", responseMap.get("version") instanceof String);
    }

    // ========== User Contract ==========

    @Test
    public void testUserContract_GetResponseContainsExpectedFields() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("johndoe");
        user.setPassword("secret");
        user.setEmail("john@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        Mockito.when(userService.findUser("1")).thenReturn(user);

        MvcResult result = mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);

        assertTrue("Response should contain 'userId' field", responseMap.containsKey("userId"));
        assertTrue("Response should contain 'username' field", responseMap.containsKey("username"));
        assertTrue("Response should contain 'password' field", responseMap.containsKey("password"));
        assertTrue("Response should contain 'email' field", responseMap.containsKey("email"));
        assertTrue("Response should contain 'firstName' field", responseMap.containsKey("firstName"));
        assertTrue("Response should contain 'secondName' field", responseMap.containsKey("secondName"));
        assertEquals("Field count should be 6", 6, responseMap.size());

        assertTrue("'userId' should be a number", responseMap.get("userId") instanceof Number);
        assertTrue("'username' should be a string", responseMap.get("username") instanceof String);
        assertTrue("'email' should be a string", responseMap.get("email") instanceof String);
    }

    // ========== Employee Contract ==========

    @Test
    public void testEmployeeContract_GetResponseContainsExpectedFields() throws Exception {
        EmployeeRole role = new EmployeeRole();
        role.setId(1);
        role.setRole("Developer");

        Employee employee = new Employee();
        employee.setId(1);
        employee.setFirstname("Jane");
        employee.setSurname("Smith");
        employee.setRole(role);

        Mockito.when(employeeService.getEmployee(1)).thenReturn(employee);

        MvcResult result = mockMvc.perform(get("/employee/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);

        assertTrue("Response should contain 'employeeId' field", responseMap.containsKey("employeeId"));
        assertTrue("Response should contain 'firstName' field", responseMap.containsKey("firstName"));
        assertTrue("Response should contain 'secondName' field", responseMap.containsKey("secondName"));
        assertTrue("Response should contain 'role' field", responseMap.containsKey("role"));
        assertEquals("Field count should be 4", 4, responseMap.size());

        assertTrue("'employeeId' should be a number", responseMap.get("employeeId") instanceof Number);
        assertTrue("'firstName' should be a string", responseMap.get("firstName") instanceof String);
        assertTrue("'secondName' should be a string", responseMap.get("secondName") instanceof String);
        assertTrue("'role' should be a string", responseMap.get("role") instanceof String);
    }

    // ========== Client Contract ==========

    @Test
    public void testClientContract_GetResponseContainsExpectedFields() throws Exception {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Acme Corp");
        client.setProjects(new java.util.HashSet<com.mycompany.entapp.snowman.domain.model.Project>());

        Mockito.when(clientService.getClient(1)).thenReturn(client);

        MvcResult result = mockMvc.perform(get("/client/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);

        assertTrue("Response should contain 'clientId' field", responseMap.containsKey("clientId"));
        assertTrue("Response should contain 'clientName' field", responseMap.containsKey("clientName"));
        assertTrue("Response should contain 'projects' field", responseMap.containsKey("projects"));
        assertEquals("Field count should be 3", 3, responseMap.size());

        assertTrue("'clientId' should be a number", responseMap.get("clientId") instanceof Number);
        assertTrue("'clientName' should be a string", responseMap.get("clientName") instanceof String);
    }

    @Test
    public void testClientContract_CreateRequestAcceptsJsonBody() throws Exception {
        Mockito.doNothing().when(clientService).createClient(Mockito.any(Client.class));

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(1);
        clientResource.setClientName("New Client");
        clientResource.setProjects(new ArrayList<com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource>());

        mockMvc.perform(post("/client/new")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientResource)))
                .andExpect(status().isOk());
    }

    // ========== Project Contract ==========

    @Test
    public void testProjectContract_GetResponseContainsExpectedFields() throws Exception {
        Date startDate = new DateTime(2018, 1, 1, 12, 0, 0).toDate();
        Date endDate = new DateTime(2019, 6, 15, 12, 0, 0).toDate();

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Test Project");
        project.setDateStarted(startDate);
        project.setDateEnded(endDate);

        Mockito.when(projectService.getProject(1)).thenReturn(project);

        MvcResult result = mockMvc.perform(get("/project/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);

        assertTrue("Response should contain 'projectId' field", responseMap.containsKey("projectId"));
        assertTrue("Response should contain 'title' field", responseMap.containsKey("title"));
        assertTrue("Response should contain 'dateStarted' field", responseMap.containsKey("dateStarted"));
        assertTrue("Response should contain 'dateEnded' field", responseMap.containsKey("dateEnded"));
        assertEquals("Field count should be 4", 4, responseMap.size());

        assertTrue("'projectId' should be a number", responseMap.get("projectId") instanceof Number);
        assertTrue("'title' should be a string", responseMap.get("title") instanceof String);
        assertNotNull("'dateStarted' should not be null", responseMap.get("dateStarted"));
        assertNotNull("'dateEnded' should not be null", responseMap.get("dateEnded"));
    }

    // ========== Content-Type Negotiation ==========

    @Test
    public void testAppInfo_ReturnsJsonContentType() throws Exception {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(1);
        appInfo.setVersion("1.0.0");
        Mockito.when(applicationInfoService.getAppInfo()).thenReturn(appInfo);

        MvcResult result = mockMvc.perform(get("/app/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertNotNull("Content-Type should not be null", contentType);
        assertTrue("Content-Type should be JSON", contentType.contains("application/json"));
    }

    @Test
    public void testUser_ReturnsJsonContentType() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("test");
        user.setPassword("pass");
        user.setEmail("test@test.com");
        user.setFirstname("Test");
        user.setLastname("User");

        Mockito.when(userService.findUser("1")).thenReturn(user);

        MvcResult result = mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertNotNull("Content-Type should not be null", contentType);
        assertTrue("Content-Type should be JSON", contentType.contains("application/json"));
    }
}
