/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRestEndpointITest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestEndpoint userRestEndpoint;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userRestEndpoint).build();
    }

    @Test
    public void testGetUser_ReturnsOkWithJson() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setPassword("testpass");
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.password").value("testpass"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.secondName").value("Doe"));

        verify(userService).findUser("1");
    }

    @Test
    public void testGetUser_WithDifferentUserId() throws Exception {
        User user = new User();
        user.setUserId(42);
        user.setUsername("anotheruser");
        user.setPassword("anotherpass");
        user.setEmail("another@example.com");
        user.setFirstname("Jane");
        user.setLastname("Smith");

        when(userService.findUser("42")).thenReturn(user);

        mockMvc.perform(get("/user/42"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.userId").value(42))
            .andExpect(jsonPath("$.username").value("anotheruser"));

        verify(userService).findUser("42");
    }

    @Test
    public void testCreateNewUser_ReturnsOk() throws Exception {
        doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "newuser")
                .param("password", "newpass")
                .param("email", "new@example.com")
                .param("firstName", "New")
                .param("secondName", "User"))
            .andExpect(status().isOk());

        verify(userService).createUser(Mockito.any(User.class));
    }

    @Test
    public void testUpdateExistingUser_ReturnsOk() throws Exception {
        doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "updateduser")
                .param("password", "updatedpass")
                .param("email", "updated@example.com")
                .param("firstName", "Updated")
                .param("secondName", "User"))
            .andExpect(status().isOk());

        verify(userService).updateUser(Mockito.any(User.class));
    }

    @Test
    public void testDeleteUser_ReturnsOk() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/1/delete"))
            .andExpect(status().isOk());

        verify(userService).deleteUser(1);
    }

    @Test
    public void testGetUser_VerifiesJsonContentType() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setPassword("testpass");
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testDeleteUser_WithWrongHttpMethod() throws Exception {
        mockMvc.perform(post("/user/1/delete"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testGetUser_VerifiesRouting() throws Exception {
        User user = new User();
        user.setUserId(99);
        user.setUsername("routeuser");
        user.setPassword("pass");
        user.setEmail("route@test.com");
        user.setFirstname("Route");
        user.setLastname("Test");

        when(userService.findUser("99")).thenReturn(user);

        mockMvc.perform(get("/user/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(99));
    }
}
