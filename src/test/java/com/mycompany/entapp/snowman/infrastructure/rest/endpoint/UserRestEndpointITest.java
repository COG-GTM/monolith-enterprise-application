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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
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
    public void testGetUser_shouldReturnOkWithUserResource() throws Exception {
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
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"));
    }

    @Test
    public void testCreateUser_shouldReturnOk() throws Exception {
        doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "testuser")
                .param("password", "testpass")
                .param("email", "test@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe"))
                .andExpect(status().isOk());

        verify(userService).createUser(Mockito.any(User.class));
    }

    @Test
    public void testUpdateUser_shouldReturnOk() throws Exception {
        doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "testuser")
                .param("password", "testpass")
                .param("email", "test@example.com")
                .param("firstName", "John")
                .param("secondName", "Doe"))
                .andExpect(status().isOk());

        verify(userService).updateUser(Mockito.any(User.class));
    }

    @Test
    public void testDeleteUser_shouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/1/delete"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1);
    }
}
