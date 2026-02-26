-- Create user table (not managed by Hibernate since User is not a JPA entity)
-- H2 NON_KEYWORDS=USER is set in the JDBC URL to allow unquoted 'user' table name
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(50),
    lastname VARCHAR(50),
    username VARCHAR(50),
    password VARCHAR(50),
    email VARCHAR(100)
);

-- Create app_info table (loaded via JDBC, not Hibernate)
CREATE TABLE IF NOT EXISTS app_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(50)
);

-- Insert test data for app_info (use MERGE to avoid duplicate key on re-run)
MERGE INTO app_info (id, version) KEY(id) VALUES (1, '1.0.0');

-- Insert test data for user
MERGE INTO user (id, firstname, lastname, username, password, email) KEY(id)
VALUES (1, 'John', 'Doe', 'johndoe', 'password123', 'john.doe@example.com');

-- Insert test data for employee_role (Hibernate manages the table, but we need data)
MERGE INTO employee_role (id, role) KEY(id) VALUES (1, 'Developer');

-- Insert test data for employee
MERGE INTO employee (id, firstname, surname, employee_role_id) KEY(id) VALUES (1, 'Alice', 'Smith', 1);

-- Insert test data for client
MERGE INTO client (id, client_name) KEY(id) VALUES (1, 'Acme Corp');

-- Insert test data for project
MERGE INTO project (id, project_title, date_started, date_ended, client_id) KEY(id)
VALUES (1, 'Project Alpha', '2024-01-01', '2024-12-31', 1);
