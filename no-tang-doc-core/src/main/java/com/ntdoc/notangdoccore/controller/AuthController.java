package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.config.AuthProperties;
import com.ntdoc.notangdoccore.config.KeycloakClient;
import com.ntdoc.notangdoccore.dto.keycloak.AuthExchangeRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final KeycloakClient keycloakClient;
    private final AuthProperties authProperties;

    public AuthController(KeycloakClient keycloakClient,
                          AuthProperties authProperties) {
        this.keycloakClient = keycloakClient;
        this.authProperties = authProperties;
    }

    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@RequestBody AuthExchangeRequest req) {
        try {
            // redirect 校验
            if (req.getRedirectUri() == null || authProperties.getAllowedRedirectUris() == null ||
                authProperties.getAllowedRedirectUris().stream().noneMatch(r -> r.equalsIgnoreCase(req.getRedirectUri()))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "redirect_not_allowed"));
            }
            if (req.getCode()==null || req.getCodeVerifier()==null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "missing_code_or_verifier"));
            }
            Map<String,Object> token = keycloakClient.exchangeCode(req);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Exchange failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "invalid_code"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest body) {
        if (body == null || body.getRefreshToken() == null || body.getRefreshToken().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "missing_refresh_token"));
        }
        try {
            Map<String,Object> token = keycloakClient.refresh(body.getRefreshToken());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "invalid_refresh_token"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken jwtAuth) || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "missing_token"));
        }
        Jwt jwt = jwtAuth.getToken();
        Map<String, Object> claims = jwt.getClaims();
        // 角色提取（只返回去掉前缀的 ROLE_ 部分）
        List<String> roles = jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "sub", claims.get("sub"),
                "preferred_username", claims.getOrDefault("preferred_username", claims.get("sub")),
                "email", claims.get("email"),
                "name", claims.getOrDefault("name", claims.getOrDefault("preferred_username", claims.get("sub"))),
                "roles", roles,
                "claims", claims
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest body) {
        try {
            String refreshToken = body != null ? body.getRefreshToken() : null;
            String idToken = body != null ? body.getIdToken() : null;
            if (refreshToken == null && idToken == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", "missing_refresh_or_id_token"));
            }
            keycloakClient.logout(refreshToken, idToken);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.warn("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "logout_failed"));
        }
    }

    public static class RefreshRequest {
        private String refreshToken;
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
    public static class LogoutRequest {
        private String refreshToken; private String idToken;
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getIdToken() { return idToken; }
        public void setIdToken(String idToken) { this.idToken = idToken; }
    }
}