/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
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

@SuppressWarnings("deprecation")
public class UserRestEndpointITest {

    private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON_UTF8;

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
    public void testGetUserReturnsOkWithJsonBody() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("johndoe");
        user.setPassword("secret");
        user.setEmail("john@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.password").value("secret"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"));
    }

    @Test
    public void testCreateNewUserReturnsOk() throws Exception {
        doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "johndoe")
                .param("password", "secret")
                .param("email", "john@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(userService).createUser(Mockito.any(User.class));
    }

    @Test
    public void testUpdateExistingUserReturnsOk() throws Exception {
        doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "johndoe")
                .param("password", "newsecret")
                .param("email", "john@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(userService).updateUser(Mockito.any(User.class));
    }

    @Test
    public void testDeleteUserReturnsOk() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/1/delete"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1);
    }

    @Test
    public void testGetUserReturnsJsonContentType() throws Exception {
        User user = new User();
        user.setUserId(42);
        user.setUsername("janedoe");
        user.setPassword("pass");
        user.setEmail("jane@example.com");
        user.setFirstname("Jane");
        user.setLastname("Doe");

        when(userService.findUser("42")).thenReturn(user);

        mockMvc.perform(get("/user/42")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }
}
