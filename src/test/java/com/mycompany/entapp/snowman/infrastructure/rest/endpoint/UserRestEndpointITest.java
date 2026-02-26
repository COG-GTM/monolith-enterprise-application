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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link UserRestEndpoint}.
 *
 * <p>Exercises the full request/response cycle through MockMvc with an
 * in-memory H2 database. User data is loaded via the test-data.sql script
 * which creates the 'user' table (not a JPA entity) and inserts test rows.
 *
 * <p>Note: UserDaoImpl.saveUser() is not implemented in production code
 * (throws RuntimeException("Not Yet Implemented")), so create/update tests
 * verify that the endpoint propagates the expected error.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetUser_shouldReturnOkWithUserData() throws Exception {
        mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"));
    }

    @Test
    public void testGetUser_shouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteUser_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/user/1/delete"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateUser_shouldFailDaoNotImplemented() throws Exception {
        // UserDaoImpl.saveUser() throws RuntimeException("Not Yet Implemented")
        try {
            mockMvc.perform(post("/user/create")
                    .param("userId", "0")
                    .param("firstName", "Jane")
                    .param("secondName", "Doe")
                    .param("username", "janedoe")
                    .param("password", "pass123")
                    .param("email", "jane@example.com"));
            fail("Expected NestedServletException due to unimplemented DAO");
        } catch (NestedServletException e) {
            assertTrue(e.getCause().getMessage().contains("Not Yet Implemented"));
        }
    }

    @Test
    public void testUpdateUser_shouldFailDaoNotImplemented() throws Exception {
        // UserDaoImpl.saveUser() throws RuntimeException("Not Yet Implemented")
        try {
            mockMvc.perform(post("/user/update")
                    .param("userId", "1")
                    .param("firstName", "John")
                    .param("secondName", "Updated")
                    .param("username", "johndoe")
                    .param("password", "newpass")
                    .param("email", "john.updated@example.com"));
            fail("Expected NestedServletException due to unimplemented DAO");
        } catch (NestedServletException e) {
            assertTrue(e.getCause().getMessage().contains("Not Yet Implemented"));
        }
    }
}
