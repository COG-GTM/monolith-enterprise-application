/*
 * |-------------------------------------------------
 * | Copyright (c) 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint.config;

import com.mycompany.entapp.snowman.application.cache.ClientCacheService;
import com.mycompany.entapp.snowman.application.healthcheck.HealthCheck;
import com.mycompany.entapp.snowman.infrastructure.db.health.DBHealthCheck;
import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.management.CacheManagementRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.management.HealthCheckRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.AppInfoRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ClientRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.EmployeeRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ProjectRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.UserRestEndpoint;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Test Spring MVC configuration for REST integration tests.
 * Boots a full Spring MVC context with mocked service layer beans
 * to enable MockMvc-based HTTP request/response testing.
 */
@Configuration
@EnableWebMvc
public class TestWebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
    }

    // --- REST Endpoint Controllers ---

    @Bean
    public EmployeeRestEndpoint employeeRestEndpoint() {
        return new EmployeeRestEndpoint();
    }

    @Bean
    public ClientRestEndpoint clientRestEndpoint() {
        return new ClientRestEndpoint();
    }

    @Bean
    public ProjectRestEndpoint projectRestEndpoint() {
        return new ProjectRestEndpoint();
    }

    @Bean
    public UserRestEndpoint userRestEndpoint() {
        return new UserRestEndpoint();
    }

    @Bean
    public AppInfoRestEndpoint appInfoRestEndpoint() {
        return new AppInfoRestEndpoint();
    }

    @Bean
    public HealthCheckRestEndpoint healthCheckRestEndpoint() {
        return new HealthCheckRestEndpoint();
    }

    @Bean
    public CacheManagementRestEndpoint cacheManagementRestEndpoint() {
        return new CacheManagementRestEndpoint();
    }

    // --- Mocked Service Layer Beans ---

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
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public ApplicationInfoService applicationInfoService() {
        return Mockito.mock(ApplicationInfoService.class);
    }

    @Bean
    public HealthCheck healthCheck() {
        return Mockito.mock(HealthCheck.class);
    }

    @Bean
    public ClientCacheService clientCacheService() {
        return Mockito.mock(ClientCacheService.class);
    }

    @Bean
    public DBHealthCheck dbHealthCheck() {
        return Mockito.mock(DBHealthCheck.class);
    }
}
