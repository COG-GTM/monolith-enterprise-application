package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.integration;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.UserRestEndpoint;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void testGetUser_ReturnsOkWithUserData() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("jdoe");
        user.setEmail("jdoe@test.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.email").value("jdoe@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.secondName").value("Doe"));

        verify(userService).findUser("1");
    }

    @Test
    public void testCreateNewUser_ReturnsOk() throws Exception {
        doNothing().when(userService).createUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/create")
                .param("userId", "0")
                .param("username", "newuser")
                .param("password", "pass123")
                .param("email", "new@test.com")
                .param("firstName", "New")
                .param("secondName", "User"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateExistingUser_ReturnsOk() throws Exception {
        doNothing().when(userService).updateUser(Mockito.any(User.class));

        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "jdoe")
                .param("password", "newpass")
                .param("email", "jdoe@updated.com")
                .param("firstName", "John")
                .param("secondName", "Updated"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteUser_ReturnsOk() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/{userId}/delete", 1))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1);
    }

    @Test
    public void testGetUser_VerifiesServiceInteraction() throws Exception {
        User user = new User();
        user.setUserId(42);
        user.setUsername("testuser");

        when(userService.findUser("42")).thenReturn(user);

        mockMvc.perform(get("/user/{userId}", "42")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).findUser("42");
        Mockito.verifyNoMoreInteractions(userService);
    }
}
