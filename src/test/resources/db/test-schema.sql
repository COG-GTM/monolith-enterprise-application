-- Schema for non-Hibernate managed tables (User table)
-- Hibernate-managed entities (Employee, Client, Project, etc.) are created by hbm2ddl.auto=create-drop

CREATE TABLE IF NOT EXISTS `user` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20),
    password VARCHAR(20),
    email VARCHAR(20),
    firstname VARCHAR(20),
    lastname VARCHAR(20)
);
