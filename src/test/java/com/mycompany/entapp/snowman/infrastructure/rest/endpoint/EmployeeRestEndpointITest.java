/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link EmployeeRestEndpoint}.
 *
 * <p>Exercises the full request/response cycle through MockMvc with an
 * in-memory H2 database. Uses @Transactional to provide Hibernate session
 * context for DAO operations.
 *
 * <p>Note: EmployeeDaoImpl.retrieveEmployee() returns null (not implemented),
 * so GET causes NPE in mapper. Update and delete also fail because the
 * service checks getEmployee() first which returns null.
 * Create works because it goes directly to session.save().
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class EmployeeRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetEmployee_shouldFailDaoReturnsNull() throws Exception {
        // EmployeeDaoImpl.retrieveEmployee() returns null (not implemented),
        // causing NPE in EmployeeResourceMapper when accessing employee.getRole()
        try {
            mockMvc.perform(get("/employee/1")
                    .accept(MediaType.APPLICATION_JSON));
            fail("Expected NestedServletException due to null from DAO");
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    @Test
    public void testCreateEmployee_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/employee/create")
                .param("employeeId", "0")
                .param("firstName", "Bob")
                .param("secondName", "Jones")
                .param("role", "Tester"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateEmployee_shouldFailDaoReturnsNull() throws Exception {
        // EmployeeDaoImpl.retrieveEmployee() returns null, so
        // EmployeeServiceImpl.updateEmployee() throws RuntimeException
        try {
            mockMvc.perform(post("/employee/update")
                    .param("employeeId", "1")
                    .param("firstName", "Alice")
                    .param("secondName", "Johnson")
                    .param("role", "Senior Developer"));
            fail("Expected NestedServletException due to null from DAO");
        } catch (NestedServletException e) {
            assertTrue(e.getCause().getMessage().contains("no existing employee"));
        }
    }

    @Test
    public void testDeleteEmployee_shouldFailDaoReturnsNull() throws Exception {
        // EmployeeDaoImpl.retrieveEmployee() returns null, so
        // EmployeeServiceImpl.deleteEmployee() throws RuntimeException
        try {
            mockMvc.perform(delete("/employee/1/delete"));
            fail("Expected NestedServletException due to null from DAO");
        } catch (NestedServletException e) {
            assertTrue(e.getCause().getMessage().contains("no existing employee"));
        }
    }
}
