# no-tang-doc-core

> 模块：后端核心服务（Spring Boot + Keycloak + JPA + Liquibase）。本地仅存用户业务属性，不存密码，认证全部交给 Keycloak。

## 技术栈
- Java 24
- Spring Boot 3.5.x (Web / Security / OAuth2 Resource Server & Client / JPA / Actuator)
- Keycloak 26.x
- MySQL 8
- Liquibase
- OpenAI SDK

## 快速启动步骤
1. 准备 Docker Desktop
2. Docker 启动 MySQL 并建库
3. Docker 启动 Keycloak
4. Keycloak 中创建 Realm / Role / Client
5. 将 Client Secret & OpenAI Key配置到application.yaml 或环境变量
6. 启动应用（Liquibase 初始化表）
7. 注册用户 & 登录验证

---
### 1. 数据库
```bash
docker run -p 3305:3306 --name ntdoc-core -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=notangdoc -d mysql
```
将MySql容器映射到本地3305端口，root密码root，创建notangdoc数据库。

```bash
### 2. 启动 Keycloak
```bash
docker run -p 127.0.0.1:8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.3.4 start-dev
```
Keycloak 26.x 使用 start-dev 开发模式，内置 H2 数据库，重启数据不保留。
访问控制台：http://localhost:8080/admin  (admin / admin)

### 3. Keycloak 初始化
- 创建 Realm: ntdoc
- 开启用户注册 Realm Settings -> Login -> User registration
- 创建角色：USER, ADMIN
- 创建 Client:
  - Client ID: no-tang-doc-core
  - OpenID Connect, 勾选 Client Authentication (confidential)
  - Standard Flow: ON, Direct Access Grants: OFF
  - Redirect URIs: `http://localhost:8070/login/oauth2/code/*`
  - Web Origins: `http://localhost:8070`
  - 保存后在 Credentials 页复制 Client Secret

### 4. 关键 application.yaml 片段（已存在，核对即可）
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ntdoc
      client:
        registration:
          keycloak:
            client-id: no-tang-doc-core
            authorization-grant-type: authorization_code
            redirect-uri: "http://localhost:8070/login/oauth2/code/*"
            scope: openid,profile,email
            client-secret: ${KEYCLOAK_CLIENT_SECRET:修改为你自己本地的Client secret} # 修改为你本地KeyClock的Client Secret
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/ntdoc
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:sk-xxxxxx} # 修改为你自己的OpenAI Key
      chat:
        options:
          model: gpt-4.1-mini
          temperature: 0.2
keycloak:
  realm: ntdoc
  auth-server-url: http://localhost:8080
  admin:
    realm: master
    username: ${KEYCLOAK_ADMIN_USERNAME:admin}
    password: ${KEYCLOAK_ADMIN_PASSWORD:admin}
  client:
    id: no-tang-doc-core
    secret: ${KEYCLOAK_CLIENT_SECRET:改成你自己的KeyCloak的Client Secret}
```

### 5. 启动应用
```bash
mvn spring-boot:run
```
访问：http://localhost:8070
首次启动 Liquibase 创建将自动创建表：app_user, dbchangelog, dbchangelog-lock等

### 6. 注册用户
接口：`POST /api/public/register`
```bash
curl -X POST http://localhost:8070/api/public/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com","password":"password","roles":["USER"]}'
```
返回包含 keycloakUserId。
注册用户的信息会被存入mysql数据库, 但是Credential全部交给Keycloak管理。

### 7. 获取 Token
使用Insomnia进行OAuth2登录和测试受保护接口. 下载其中一个
Insomnia: https://insomnia.rest/
去Teams NoTangGroup文件中下载insomnia export文件，并导入Insomnia。
修改Today's Weather API的Auth方法为None, 尝试调用接口.
再将Today's Weather API的Auth方法设置为Inherit Auth from Parent, 再次调用接口，成功返回结果。


