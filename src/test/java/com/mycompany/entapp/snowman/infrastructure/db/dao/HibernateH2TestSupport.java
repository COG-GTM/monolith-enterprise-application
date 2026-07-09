/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.EmployeeProject;
import com.mycompany.entapp.snowman.domain.model.EmployeeRole;
import com.mycompany.entapp.snowman.domain.model.Project;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test-only helper that bootstraps a Hibernate {@link SessionFactory} backed by an in-memory
 * H2 database. Each invocation of {@link #buildSessionFactory()} uses a unique in-memory schema
 * so that DAO integration tests remain isolated from one another.
 */
public final class HibernateH2TestSupport {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    private HibernateH2TestSupport() {
    }

    public static SessionFactory buildSessionFactory() {
        String dbName = "snowman_it_" + DB_COUNTER.incrementAndGet();

        Properties settings = new Properties();
        settings.put("hibernate.connection.driver_class", "org.h2.Driver");
        settings.put("hibernate.connection.url",
            "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL");
        settings.put("hibernate.connection.username", "sa");
        settings.put("hibernate.connection.password", "");
        // hibernate-c3p0 is on the classpath but its version is mismatched with hibernate-core;
        // force the built-in DriverManager provider so the c3p0 pool is not auto-selected.
        settings.put("hibernate.connection.provider_class",
            "org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl");
        settings.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        settings.put("hibernate.hbm2ddl.auto", "create-drop");
        settings.put("hibernate.current_session_context_class", "thread");
        settings.put("hibernate.show_sql", "false");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .applySettings(settings)
            .build();

        try {
            return new MetadataSources(registry)
                .addAnnotatedClass(EmployeeRole.class)
                .addAnnotatedClass(Employee.class)
                .addAnnotatedClass(Client.class)
                .addAnnotatedClass(Project.class)
                .addAnnotatedClass(EmployeeProject.class)
                .buildMetadata()
                .buildSessionFactory();
        } catch (RuntimeException e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    /**
     * Injects the given {@link SessionFactory} into the private {@code sessionFactory} field
     * declared on {@link AbstractHibernateDao}.
     */
    public static void injectSessionFactory(AbstractHibernateDao dao, SessionFactory sessionFactory) {
        try {
            Field field = AbstractHibernateDao.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            field.set(dao, sessionFactory);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to inject SessionFactory into DAO", e);
        }
    }
}
