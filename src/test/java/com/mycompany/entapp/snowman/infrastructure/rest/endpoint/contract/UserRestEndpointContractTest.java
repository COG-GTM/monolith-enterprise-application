package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.contract;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.UserRestEndpoint;
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
 * Contract tests for User REST endpoint.
 * Validates that the JSON response structure conforms to the expected API contract.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRestEndpointContractTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestEndpoint userRestEndpoint;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userRestEndpoint).build();
    }

    @Test
    public void testGetUser_ResponseContainsRequiredFields() throws Exception {
        User user = createTestUser();

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.secondName").exists());
    }

    @Test
    public void testGetUser_ResponseFieldTypes() throws Exception {
        User user = createTestUser();

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.username").isString())
                .andExpect(jsonPath("$.password").isString())
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.firstName").isString())
                .andExpect(jsonPath("$.secondName").isString());
    }

    @Test
    public void testGetUser_ResponseHasNoUnexpectedFields() throws Exception {
        User user = createTestUser();

        when(userService.findUser("1")).thenReturn(user);

        MvcResult result = mockMvc.perform(get("/user/{userId}", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertTrue("Response should contain userId", json.contains("\"userId\""));
        assertTrue("Response should contain username", json.contains("\"username\""));
        assertTrue("Response should contain email", json.contains("\"email\""));
        assertTrue("Response should contain firstName", json.contains("\"firstName\""));
        assertTrue("Response should contain secondName", json.contains("\"secondName\""));
        assertFalse("Response should not expose internal firstname field", json.contains("\"firstname\""));
        assertFalse("Response should not expose internal lastname field", json.contains("\"lastname\""));
    }

    @Test
    public void testGetUser_ContentTypeIsJson() throws Exception {
        User user = createTestUser();

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void testGetUser_FieldValuesMappedCorrectly() throws Exception {
        User user = createTestUser();

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.email").value("jdoe@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"));
    }

    private User createTestUser() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("jdoe");
        user.setPassword("secret");
        user.setEmail("jdoe@test.com");
        user.setFirstname("John");
        user.setLastname("Doe");
        return user;
    }
}
