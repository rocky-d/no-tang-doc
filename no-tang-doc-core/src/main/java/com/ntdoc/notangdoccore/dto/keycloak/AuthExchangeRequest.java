package com.ntdoc.notangdoccore.dto.keycloak;

import lombok.Data;

@Data
public class AuthExchangeRequest {
    private String code;
    private String codeVerifier;
    private String redirectUri;
    private String nonce;


}