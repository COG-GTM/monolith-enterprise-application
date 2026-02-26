/*
 * |-------------------------------------------------
 * | Copyright 2024 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
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

import static org.mockito.Matchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestEndpointTestConfig.class)
public class UserRestEndpointITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(userService);
    }

    @Test
    public void testGetUser_ReturnsOkWithJson() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("johndoe");
        user.setPassword("secret");
        user.setEmail("john@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        Mockito.when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test(expected = Exception.class)
    public void testGetUser_ServiceReturnsNull_ThrowsNpe() throws Exception {
        Mockito.when(userService.findUser(anyString())).thenReturn(null);

        mockMvc.perform(get("/user/999")
                .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateUser_ReturnsOk() throws Exception {
        Mockito.doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "johndoe")
                .param("password", "secret")
                .param("email", "john@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUser_ReturnsOk() throws Exception {
        Mockito.doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "johndoe")
                .param("password", "newsecret")
                .param("email", "john@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteUser_ReturnsOk() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/1/delete"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetUser_CorrectUrlMapping() throws Exception {
        User user = new User();
        Mockito.when(userService.findUser("42")).thenReturn(user);

        mockMvc.perform(get("/user/42"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteUser_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/user/1/delete"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testGetUser_ResponseContainsAllFields() throws Exception {
        User user = new User();
        user.setUserId(5);
        user.setUsername("testuser");
        user.setPassword("pass123");
        user.setEmail("test@test.com");
        user.setFirstname("Test");
        user.setLastname("User");

        Mockito.when(userService.findUser("5")).thenReturn(user);

        mockMvc.perform(get("/user/5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.password").value("pass123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.secondName").value("User"));
    }
}
