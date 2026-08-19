/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.repository.impl;

import com.mycompany.entapp.snowman.application.cache.impl.ClientCacheServiceImpl;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.repository.ClientRepository;
import com.mycompany.entapp.snowman.infrastructure.cache.ClientCachePort;
import com.mycompany.entapp.snowman.infrastructure.cache.impl.ClientCacheAdapter;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ClientDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

/**
 * Integration test of the declarative caching applied to the client repository. Runs a real
 * Spring context with caching enabled against an in-memory cache manager, so the cache
 * annotations (names, keys and eviction) are exercised rather than mocked away.
 */
public class ClientRepositoryCachingITest {

    private static final int CLIENT_ID = 1;
    private static final String CACHE_NAME = "clientFindCache";

    private AnnotationConfigApplicationContext context;
    private ClientRepository clientRepository;
    private ClientDao clientDao;

    @Configuration
    @EnableCaching
    static class CachingTestConfig {

        @Bean
        public ConcurrentMapCacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CACHE_NAME);
        }

        @Bean
        public ClientDao clientDao() {
            return Mockito.mock(ClientDao.class);
        }

        @Bean
        public ClientCacheAdapter clientCacheAdapter() {
            return new ClientCacheAdapter();
        }

        @Bean
        public ClientRepository clientRepository() {
            ClientRepositoryImpl clientRepository = new ClientRepositoryImpl();
            ReflectionTestUtils.setField(clientRepository, "clientDao", clientDao());
            return clientRepository;
        }
    }

    @Before
    public void setUp() {
        context = new AnnotationConfigApplicationContext(CachingTestConfig.class);
        clientRepository = context.getBean(ClientRepository.class);
        clientDao = context.getBean(ClientDao.class);
    }

    @Test
    public void testGetClientShouldOnlyHitTheDaoOnce() {
        Client client = client();
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client);

        assertEquals(client, clientRepository.getClient(CLIENT_ID));
        assertEquals(client, clientRepository.getClient(CLIENT_ID));

        Mockito.verify(clientDao, Mockito.times(1)).getClient(CLIENT_ID);
        assertEquals(client, context.getBean(ConcurrentMapCacheManager.class).getCache(CACHE_NAME).get(CLIENT_ID).get());
    }

    @Test
    public void testDeleteClientShouldEvictCachedClient() {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());

        clientRepository.getClient(CLIENT_ID);
        clientRepository.deleteClient(CLIENT_ID);
        clientRepository.getClient(CLIENT_ID);

        Mockito.verify(clientDao, Mockito.times(2)).getClient(CLIENT_ID);
    }

    @Test
    public void testUpdateClientShouldEvictStaleCachedClient() {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());
        clientRepository.getClient(CLIENT_ID);

        Client updatedClient = client();
        updatedClient.setClientName("New Name");
        clientRepository.updateClient(updatedClient);

        clientRepository.getClient(CLIENT_ID);

        Mockito.verify(clientDao, Mockito.times(2)).getClient(CLIENT_ID);
    }

    @Test
    public void testClearCacheShouldEvictAllEntries() {
        Mockito.when(clientDao.getClient(CLIENT_ID)).thenReturn(client());
        clientRepository.getClient(CLIENT_ID);

        ClientCacheServiceImpl clientCacheService = new ClientCacheServiceImpl();
        ReflectionTestUtils.setField(clientCacheService, "clientCachePort", context.getBean(ClientCachePort.class));
        clientCacheService.clearCache();

        clientRepository.getClient(CLIENT_ID);

        Mockito.verify(clientDao, Mockito.times(2)).getClient(CLIENT_ID);
    }

    private Client client() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setClientName("Client");
        return client;
    }
}
