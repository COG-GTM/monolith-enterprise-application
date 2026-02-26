-- Schema for non-JPA entities (User, AppInfo) that Hibernate won't auto-create
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(50),
    lastname VARCHAR(50),
    username VARCHAR(50),
    password VARCHAR(100),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS app_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(50)
);
