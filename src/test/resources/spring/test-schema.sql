-- H2-compatible schema for DAO integration tests

-- Hibernate sequence used by @GeneratedValue(strategy = AUTO)
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS employee_role (
    id INT PRIMARY KEY,
    role VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS employee (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(20) NOT NULL,
    surname VARCHAR(20) NOT NULL,
    employee_role_id INT,
    FOREIGN KEY (employee_role_id) REFERENCES employee_role(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS client (
    id INT PRIMARY KEY AUTO_INCREMENT,
    client_name VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS project (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_title VARCHAR(20) NOT NULL,
    date_started DATE NOT NULL,
    date_ended DATE,
    client_id INT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_project (
    employee_id INT,
    project_id INT,
    date_started DATE,
    date_ended DATE,
    PRIMARY KEY (employee_id, project_id)
);

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20),
    password VARCHAR(20),
    email VARCHAR(20),
    firstname VARCHAR(20),
    lastname VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS app_info (
    id INT PRIMARY KEY,
    version VARCHAR(20)
);
