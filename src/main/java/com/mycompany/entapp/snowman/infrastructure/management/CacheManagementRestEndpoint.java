/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management;

import com.mycompany.entapp.snowman.application.cache.ClientCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/cache")
public class CacheManagementRestEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagementRestEndpoint.class);

    static final String CLIENT_CACHE = "clientFindCache";

    private static final Set<String> CLEARABLE_CACHES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(CLIENT_CACHE)));

    @Autowired
    private ClientCacheService clientCacheService;

    @RequestMapping(value = "/{cacheName}/clear", method = RequestMethod.POST)
    public ResponseEntity<CacheManagementResource> clearClientCache(@PathVariable("cacheName") String cacheName){
        if (!CLEARABLE_CACHES.contains(cacheName)) {
            LOGGER.warn("Rejected request to clear an unknown cache");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new CacheManagementResource(HttpStatus.NOT_FOUND.value(), "Unknown cache"));
        }
        LOGGER.info("About to clear {} cache", cacheName);
        clientCacheService.clearCache();
        return ResponseEntity.ok(
            new CacheManagementResource(HttpStatus.OK.value(), cacheName + " have been cleared"));
    }

    /**
     * Private inner class?
     */
    private class CacheManagementResource {
        private int statusCode;
        private String description;

        public CacheManagementResource(int statusCode, String description) {
            this.statusCode = statusCode;
            this.description = description;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
