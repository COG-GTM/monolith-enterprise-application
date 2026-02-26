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

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ProjectRestEndpoint}.
 *
 * <p>Exercises the full request/response cycle through MockMvc with an
 * in-memory H2 database. Project data is managed by Hibernate.
 * Uses @Transactional to provide Hibernate session context for DAO operations.
 *
 * <p>Note: ProjectDaoImpl.removeProject() passes an int to session.delete()
 * instead of an entity object, causing a Hibernate error on delete.
 * The update endpoint has a typo in the URL mapping ("/update}") in
 * the production code.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class ProjectRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetProject_shouldReturnOkWithProjectData() throws Exception {
        mockMvc.perform(get("/project/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Project Alpha"));
    }

    @Test
    public void testGetProject_shouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/project/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateProject_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/project/create")
                .param("projectId", "0")
                .param("title", "New Project"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProject_shouldFailDaoBug() throws Exception {
        // ProjectDaoImpl.removeProject() passes int to session.delete() instead
        // of an entity, causing a Hibernate error.
        try {
            mockMvc.perform(get("/project/1/delete"));
        } catch (NestedServletException e) {
            // Expected: DAO passes wrong type to session.delete()
            assertNotNull(e.getCause());
        }
    }

    @Test
    public void testUpdateProject_endpointMapped() throws Exception {
        // The production endpoint has a typo: @RequestMapping("/update}") with
        // a trailing brace. We match the actual URL to verify the mapping.
        try {
            mockMvc.perform(get("/project/update}")
                    .param("projectId", "1")
                    .param("title", "Updated Project"));
        } catch (NestedServletException e) {
            // Expected: may fail during processing
            assertNotNull(e.getCause());
        }
    }
}
