-- Creates the Snowman application account. No credentials are stored here: set them for the
-- session before sourcing this script, e.g. from the mysql client connected as an admin:
--   SET @snowman_db_username = 'snowman_app';
--   SET @snowman_db_password = '<strong unique password>';
--   SOURCE src/main/resources/db/scripts/init-db.sql;
-- Use the same values for SNOWMAN_DB_USERNAME / SNOWMAN_DB_PASSWORD when running the application.

CREATE DATABASE IF NOT EXISTS snowman;

SET @create_user = CONCAT('CREATE USER ', QUOTE(@snowman_db_username), '@''localhost'' IDENTIFIED BY ', QUOTE(@snowman_db_password));
PREPARE stmt FROM @create_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @grant_user = CONCAT('GRANT ALL PRIVILEGES ON snowman.* TO ', QUOTE(@snowman_db_username), '@''localhost''');
PREPARE stmt FROM @grant_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_user = NULL;
SET @snowman_db_password = NULL;

FLUSH PRIVILEGES;

USE snowman;
