CREATE DATABASE IF NOT EXISTS snowman;

CREATE USER 'username'@'localhost' IDENTIFIED BY 'password';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
    ON snowman.* TO 'username'@'localhost';

FLUSH PRIVILEGES;

USE snowman;
