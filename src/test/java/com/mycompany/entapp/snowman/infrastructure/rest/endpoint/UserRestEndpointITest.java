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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class UserRestEndpointITest {

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
    public void testGetUser() throws Exception {
        String userId = "1";
        User user = createUser(1);

        when(userService.findUser(userId)).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId", is(1)))
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")))
            .andExpect(jsonPath("$.firstName", is("John")))
            .andExpect(jsonPath("$.secondName", is("Doe")));

        verify(userService, times(1)).findUser(userId);
    }

    @Test
    public void testCreateNewUser() throws Exception {
        doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "testuser")
                .param("password", "secret")
                .param("email", "test@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe"))
            .andExpect(status().isOk());

        verify(userService, times(1)).createUser(Mockito.any(User.class));
    }

    @Test
    public void testUpdateExistingUser() throws Exception {
        doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "testuser")
                .param("password", "secret")
                .param("email", "updated@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe"))
            .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(Mockito.any(User.class));
    }

    @Test
    public void testDeleteUser() throws Exception {
        int userId = 1;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/user/{userId}/delete", userId))
            .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(userId);
    }

    private User createUser(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testuser");
        user.setPassword("secret");
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");
        return user;
    }
}
