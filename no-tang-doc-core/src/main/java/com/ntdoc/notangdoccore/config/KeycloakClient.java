package com.ntdoc.notangdoccore.config;

import com.ntdoc.notangdoccore.dto.keycloak.AuthExchangeRequest;
import com.ntdoc.notangdoccore.exception.KeycloakClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class KeycloakClient {
    private final ClientRegistration clientRegistration;
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authCodeClient;
    private final OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenClient;
    private final RestTemplate restTemplate;

    private final String issuerUri;
    private final String clientId;
    private final String clientSecret;

    public KeycloakClient(ClientRegistrationRepository registrations,
                          @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
                          @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret,
                          @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUri) {
        log.info("[KeycloakClient:init] Starting initialization. issuerUri={}, incomingClientId={}", issuerUri, clientId);
        ClientRegistration reg = registrations.findByRegistrationId("keycloak");
        if (reg == null) {
            log.error("[KeycloakClient:init] Client registration 'keycloak' not found – aborting initialization");
            throw new IllegalStateException("Client registration 'keycloak' not found");
        }
        this.clientRegistration = reg;
        this.authCodeClient = new RestClientAuthorizationCodeTokenResponseClient();
        this.refreshTokenClient = new RestClientRefreshTokenTokenResponseClient();
        this.restTemplate = new RestTemplate();
        this.issuerUri = issuerUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        log.info("[KeycloakClient:init] Initialized. providerAuthUri={}, tokenUri={}, scopes={}, clientSecretPresent={}",
                reg.getProviderDetails().getAuthorizationUri(),
                reg.getProviderDetails().getTokenUri(),
                reg.getScopes(),
                clientSecret != null && !clientSecret.isBlank());
    }

    public Map<String,Object> exchangeCode(AuthExchangeRequest req) {
        log.info("[exchangeCode:start] Enter. requestNull={} codePresent={} redirectUri={}", req == null, req != null && req.getCode() != null, req != null ? req.getRedirectUri() : null);
        Objects.requireNonNull(req, "request must not be null");
        if (req.getCode() == null || req.getCodeVerifier() == null || req.getRedirectUri() == null) {
            log.warn("[exchangeCode:validate] Missing parameter. codeNull={} codeVerifierNull={} redirectNull={}", req.getCode() == null, req.getCodeVerifier() == null, req.getRedirectUri() == null);
            throw new KeycloakClientException("invalid_request","Missing code/code_verifier/redirect_uri",400, Map.of());
        }
        log.info("[exchangeCode:build] code={} (masked) codeVerifierLen={} redirectUri={}", safe(req.getCode()), req.getCodeVerifier().length(), req.getRedirectUri());
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .clientId(clientRegistration.getClientId())
                .redirectUri(req.getRedirectUri())
                .scopes(clientRegistration.getScopes())
                .state("ignored")
                .attributes(attrs -> attrs.put(PkceParameterNames.CODE_VERIFIER, req.getCodeVerifier()))
                .build();
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(req.getCode())
                .redirectUri(req.getRedirectUri())
                .state("ignored")
                .build();
        OAuth2AuthorizationExchange exchange = new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse);
        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(clientRegistration, exchange);
        log.info("[exchangeCode:tokenRequest] Sending authorization_code grant to tokenUri={}", clientRegistration.getProviderDetails().getTokenUri());
        try {
            OAuth2AccessTokenResponse tokenResponse = authCodeClient.getTokenResponse(grantRequest);
            logTokenResponse("exchangeCode:success", tokenResponse);
            return toMap(tokenResponse);
        } catch (OAuth2AuthorizationException ex) {
            OAuth2Error err = ex.getError();
            log.error("[exchangeCode:error] grant=authorization_code error={} description={}",
                    err != null ? err.getErrorCode() : null,
                    err != null ? err.getDescription() : ex.getMessage());
            throw wrap("authorization_code", ex);
        } finally {
            log.info("[exchangeCode:end] Completed code exchange flow");
        }
    }

    public Map<String,Object> refresh(String refreshTokenValue) {
        log.info("[refresh:start] Enter. tokenPresent={} tokenMasked={}", refreshTokenValue != null && !refreshTokenValue.isBlank(), safe(refreshTokenValue));
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            log.warn("[refresh:validate] Missing refresh_token");
            throw new KeycloakClientException("invalid_request","Missing refresh_token",400, Map.of());
        }
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(refreshTokenValue, Instant.now().minusSeconds(60));
        OAuth2AccessToken dummyAccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "expired", Instant.now().minusSeconds(3600), Instant.now().minusSeconds(300));
        OAuth2RefreshTokenGrantRequest refreshRequest = new OAuth2RefreshTokenGrantRequest(
                clientRegistration, dummyAccessToken, refreshToken);
        log.info("[refresh:tokenRequest] Sending refresh_token grant to tokenUri={}", clientRegistration.getProviderDetails().getTokenUri());
        try {
            OAuth2AccessTokenResponse tokenResponse = refreshTokenClient.getTokenResponse(refreshRequest);
            logTokenResponse("refresh:success", tokenResponse);
            return toMap(tokenResponse);
        } catch (OAuth2AuthorizationException ex) {
            OAuth2Error err = ex.getError();
            log.error("[refresh:error] grant=refresh_token error={} description={}",
                    err != null ? err.getErrorCode() : null,
                    err != null ? err.getDescription() : ex.getMessage());
            throw wrap("refresh_token", ex);
        } finally {
            log.info("[refresh:end] Completed refresh flow");
        }
    }

    public void logout(String refreshToken, String idToken) {
        log.info("[logout:start] Enter. refreshTokenPresent={} idTokenPresent={}", refreshToken != null && !refreshToken.isBlank(), idToken != null && !idToken.isBlank());
        String logoutEndpoint = issuerUri + "/protocol/openid-connect/logout";
        log.info("logout endpoint: {}", logoutEndpoint);
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
        boolean usingRefresh = false;
        if (refreshToken != null && !refreshToken.isBlank()) {
            form.add("refresh_token", refreshToken);
            usingRefresh = true;
        } else if (idToken != null && !idToken.isBlank()) {
            form.add("id_token_hint", idToken);
        } else {
            log.warn("[logout:validate] Both refreshToken & idToken are blank – nothing to send");
        }
        log.info("[logout:build] endpoint={} method=POST using={} formKeys={}", logoutEndpoint, usingRefresh ? "refresh_token" : "id_token_hint", form.keySet());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(form, headers);
        try {
            restTemplate.postForEntity(logoutEndpoint, entity, Void.class);
            log.info("[logout:success] Logout request accepted by server");
        } catch (Exception e) {
            log.error("[logout:error] Logout request failed: {}", e.getMessage());
        } finally {
            log.info("[logout:end] Completed logout flow");
        }
    }

    private void logTokenResponse(String phase, OAuth2AccessTokenResponse resp) {
        if (resp == null) {
            log.warn("[{}] Token response is null", phase);
            return;
        }
        long expiresIn = -1;
        if (resp.getAccessToken().getExpiresAt() != null && resp.getAccessToken().getIssuedAt() != null) {
            expiresIn = Duration.between(resp.getAccessToken().getIssuedAt(), resp.getAccessToken().getExpiresAt()).getSeconds();
        }
        log.info("[{}] accessTokenType={} accessTokenExpirySec={} hasRefreshToken={} additionalParamKeys={}",
                phase,
                resp.getAccessToken().getTokenType().getValue(),
                expiresIn,
                resp.getRefreshToken() != null,
                resp.getAdditionalParameters().keySet());
    }

    private String safe(String value) {
        if (value == null) return "null";
        int len = value.length();
        int show = Math.min(8, len);
        return value.substring(0, show) + "*** (len=" + len + ")";
    }

    private Map<String,Object> toMap(OAuth2AccessTokenResponse resp) {
        Map<String,Object> map = new HashMap<>();
        map.put("access_token", resp.getAccessToken().getTokenValue());
        map.put("token_type", resp.getAccessToken().getTokenType().getValue());
        if (resp.getRefreshToken() != null) {
            map.put("refresh_token", resp.getRefreshToken().getTokenValue());
        }
        if (resp.getAccessToken().getExpiresAt() != null && resp.getAccessToken().getIssuedAt() != null) {
            long expiresIn = Duration.between(resp.getAccessToken().getIssuedAt(), resp.getAccessToken().getExpiresAt()).getSeconds();
            map.put("expires_in", expiresIn);
        }
        map.putAll(resp.getAdditionalParameters());
        return map;
    }

    private KeycloakClientException wrap(String grantType, OAuth2AuthorizationException ex) {
        OAuth2Error err = ex.getError();
        String errorCode = err != null ? err.getErrorCode() : "oauth2_error";
        String desc = err != null ? err.getDescription() : ex.getMessage();
        log.info("[wrap] Wrapping OAuth2AuthorizationException grantType={} errorCode={} desc={} ", grantType, errorCode, desc);
        return new KeycloakClientException(errorCode, desc, 400, Map.of("grant_type", grantType));
    }
}