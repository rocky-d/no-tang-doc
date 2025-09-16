package com.ntdoc.notangdoccore.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String realm;
    private String authServerUrl;
    private Admin admin = new Admin();
    private Client client = new Client();

    @Data
    public static class Admin {
        private String realm = "master"; // 管理 realm
        private String username;
        private String password;
        private String clientId = "admin-cli"; // 使用内置 admin-cli
    }

    @Data
    public static class Client {
        private String id;
        private String secret; // 可能为空（public client）
    }
}

