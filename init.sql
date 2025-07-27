CREATE DATABASE IF NOT EXISTS document-app;

CREATE USER 'jaehyeok'@'%' IDENTIFIED BY 'lazy!00$girl';

GRANT ALL PRIVILEGES ON mydatabase.* TO 'jaehyeok'@'%';

FLUSH PRIVILEGES;
