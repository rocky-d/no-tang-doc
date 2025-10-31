package com.ntdoc.notangdoccore.dto.keycloak;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationRequest {
    private String username;
    private String email;
    private String password;
    private List<String> roles; // 可选，如 ["USER"]
}

