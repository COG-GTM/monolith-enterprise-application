/*
 * Integration test Spring context configuration.
 * Wires up REST controllers with mock service layer beans.
 */
package com.mycompany.entapp.snowman.infrastructure.rest.config;

import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.domain.service.UserService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.mycompany.entapp.snowman.infrastructure.rest.endpoint")
public class RestIntegrationTestConfig {

    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public EmployeeService employeeService() {
        return Mockito.mock(EmployeeService.class);
    }

    @Bean
    public ClientService clientService() {
        return Mockito.mock(ClientService.class);
    }

    @Bean
    public ProjectService projectService() {
        return Mockito.mock(ProjectService.class);
    }

    @Bean
    public ApplicationInfoService applicationInfoService() {
        return Mockito.mock(ApplicationInfoService.class);
    }
}
