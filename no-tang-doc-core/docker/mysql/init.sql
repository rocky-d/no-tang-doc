-- 只创建两个空数据库,一个是Keycloak的数据库,用于Keycloak持久化
-- 一个是application的数据库,用于存储应用相关信息

-- 1. Keycloak数据库,由Keycloak自己管理,自己创建表
CREATE DATABASE IF NOT EXISTS keycloak
       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'keycloak'@'%' IDENTIFIED BY 'keycloak_pass';
GRANT ALL PRIVILEGES ON keycloak.* TO 'keycloak'@'%';

-- 2. Application数据库(Liquibase脚本管理)
CREATE DATABASE IF NOT EXISTS notangdoc
       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

FLUSH PRIVILEGES;