-- Schema for tables not managed by Hibernate (no JPA annotations)
-- The User domain model uses JdbcTemplate instead of Hibernate ORM

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY,
    username VARCHAR(20),
    password VARCHAR(20),
    email VARCHAR(20),
    firstname VARCHAR(20),
    lastname VARCHAR(20)
);
