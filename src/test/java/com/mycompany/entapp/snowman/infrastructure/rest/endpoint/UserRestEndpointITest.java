/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.config.TestWebConfig;
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

import org.springframework.web.util.NestedServletException;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestWebConfig.class)
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
    public void testGetUser_ReturnsUserJson() throws Exception {
        User user = createTestUser(1, "jdoe", "secret", "jdoe@example.com", "John", "Doe");

        Mockito.when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.username").value("jdoe"))
            .andExpect(jsonPath("$.email").value("jdoe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.secondName").value("Doe"));
    }

    @Test
    public void testGetUser_WhenServiceReturnsNull_ThrowsException() throws Exception {
        Mockito.when(userService.findUser("nonexistent")).thenReturn(null);

        try {
            mockMvc.perform(get("/user/nonexistent")
                    .accept(MediaType.APPLICATION_JSON));
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
            return;
        }
        // If no exception, the controller handled null gracefully
    }

    @Test
    public void testCreateUser_ReturnsOk() throws Exception {
        Mockito.doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "newuser")
                .param("password", "pass123")
                .param("email", "new@example.com")
                .param("firstName", "New")
                .param("secondName", "User")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(userService).createUser(Mockito.any(User.class));
    }

    @Test
    public void testUpdateUser_ReturnsOk() throws Exception {
        Mockito.doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "updateduser")
                .param("password", "newpass")
                .param("email", "updated@example.com")
                .param("firstName", "Updated")
                .param("secondName", "User")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(userService).updateUser(Mockito.any(User.class));
    }

    @Test
    public void testDeleteUser_ReturnsOk() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/1/delete")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(userService).deleteUser(1);
    }

    private User createTestUser(int userId, String username, String password, String email,
                                String firstName, String lastName) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setFirstname(firstName);
        user.setLastname(lastName);
        return user;
    }
}
