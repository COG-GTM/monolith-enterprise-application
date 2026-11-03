/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataSourceConfigUTest {

    @Test
    public void testDataSource_shouldBeBoundedConnectionPool() throws Exception {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(
            new ClassPathResource("META-INF/application-context-db.xml"));

        BeanDefinition dataSource = beanFactory.getBeanDefinition("dataSource");
        assertEquals(ComboPooledDataSource.class.getName(), dataSource.getBeanClassName());
        TypedStringValue maxPoolSizeValue =
            (TypedStringValue) dataSource.getPropertyValues().getPropertyValue("maxPoolSize").getValue();
        assertEquals("${jdbc.pool.maxSize}", maxPoolSizeValue.getValue());

        Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        int maxPoolSize = Integer.parseInt(properties.getProperty("jdbc.pool.maxSize"));
        assertTrue("pool max must be bounded below MySQL's default max_connections (151)",
            maxPoolSize > 0 && maxPoolSize < 151);
        assertTrue(Integer.parseInt(properties.getProperty("jdbc.pool.checkoutTimeoutMillis")) > 0);
    }
}
