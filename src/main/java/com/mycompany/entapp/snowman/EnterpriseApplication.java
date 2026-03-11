/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableCaching
@ImportResource({
    "classpath:META-INF/application-context.xml"
})
public class EnterpriseApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApplication.class, args);
    }
}
