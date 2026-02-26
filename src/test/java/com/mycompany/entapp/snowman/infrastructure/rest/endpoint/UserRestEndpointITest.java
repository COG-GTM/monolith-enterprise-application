/*
 * Integration tests for UserRestEndpoint using MockMvc.
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.config.RestIntegrationTestConfig;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestIntegrationTestConfig.class)
@WebAppConfiguration
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
    public void testGetUser_returnsOkWithUserJson() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setPassword("testpass");
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");

        Mockito.when(userService.findUser("1")).thenReturn(user);

        mockMvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.secondName").value("Doe"));
    }

    @Test
    public void testGetUser_verifyServiceCalled() throws Exception {
        User user = new User();
        Mockito.when(userService.findUser("42")).thenReturn(user);

        mockMvc.perform(get("/user/42").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Mockito.verify(userService).findUser("42");
    }

    @Test
    public void testCreateUser_returnsOk() throws Exception {
        mockMvc.perform(post("/user/create")
                .param("userId", "1")
                .param("username", "newuser")
                .param("password", "secret")
                .param("email", "new@example.com")
                .param("firstName", "Jane")
                .param("secondName", "Smith"))
            .andExpect(status().isOk());

        Mockito.verify(userService).createUser(Mockito.any(User.class));
    }

    @Test
    public void testUpdateUser_returnsOk() throws Exception {
        mockMvc.perform(post("/user/update")
                .param("userId", "1")
                .param("username", "updateduser")
                .param("password", "newsecret")
                .param("email", "updated@example.com")
                .param("firstName", "Updated")
                .param("secondName", "Name"))
            .andExpect(status().isOk());

        Mockito.verify(userService).updateUser(Mockito.any(User.class));
    }

    @Test
    public void testDeleteUser_returnsOk() throws Exception {
        mockMvc.perform(delete("/user/1/delete"))
            .andExpect(status().isOk());

        Mockito.verify(userService).deleteUser(1);
    }

    @Test
    public void testGetUser_returnsJsonContentType() throws Exception {
        User user = new User();
        user.setUserId(5);
        user.setUsername("contenttype");
        user.setPassword("pass");
        user.setEmail("ct@example.com");
        user.setFirstname("Content");
        user.setLastname("Type");

        Mockito.when(userService.findUser("5")).thenReturn(user);

        mockMvc.perform(get("/user/5").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetUser_invalidMethod_returns405() throws Exception {
        mockMvc.perform(post("/user/1"))
            .andExpect(status().isMethodNotAllowed());
    }
}
