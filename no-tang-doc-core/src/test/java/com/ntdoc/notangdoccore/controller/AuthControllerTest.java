package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.config.AuthProperties;
import com.ntdoc.notangdoccore.config.KeycloakClient;
import com.ntdoc.notangdoccore.dto.keycloak.AuthExchangeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/**
 * Pure unit tests for {@link AuthController} without spinning up Spring context.
 * We mock {@link KeycloakClient} and directly invoke controller methods.
 */
public class AuthControllerTest {

    private KeycloakClient keycloakClient;
    private AuthProperties authProperties;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        keycloakClient = Mockito.mock(KeycloakClient.class);
        authProperties = new AuthProperties();
        authProperties.setAllowedRedirectUris(List.of("http://localhost:3000/auth/callback"));
        controller = new AuthController(keycloakClient, authProperties);
    }

    private AuthExchangeRequest buildExchangeRequest() {
        AuthExchangeRequest r = new AuthExchangeRequest();
        r.setCode("abc123");
        r.setCodeVerifier("verifier");
        r.setRedirectUri("http://localhost:3000/auth/callback");
        r.setNonce("n1");
        return r;
    }

    @Nested
    class ExchangeTests {
        @Test
        @DisplayName("exchange: success")
        void exchangeSuccess() {
            Map<String,Object> tokens = Map.of(
                    "access_token", "at",
                    "refresh_token", "rt",
                    "expires_in", 300
            );
            when(keycloakClient.exchangeCode(any(AuthExchangeRequest.class))).thenReturn(tokens);
            ResponseEntity<?> resp = controller.exchange(buildExchangeRequest());
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertNotNull(body);
            assertEquals("at", body.get("access_token"));
            verify(keycloakClient, times(1)).exchangeCode(any());
        }

        @Test
        @DisplayName("exchange: redirect not allowed -> 400")
        void exchangeRedirectNotAllowed() {
            AuthExchangeRequest r = buildExchangeRequest();
            r.setRedirectUri("http://evil/callback");
            ResponseEntity<?> resp = controller.exchange(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("redirect_not_allowed", body.get("error"));
            verify(keycloakClient, never()).exchangeCode(any());
        }

        @Test
        @DisplayName("exchange: missing code or verifier -> 400")
        void exchangeMissingCode() {
            AuthExchangeRequest r = buildExchangeRequest();
            r.setCode(null);
            ResponseEntity<?> resp = controller.exchange(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("missing_code_or_verifier", body.get("error"));
        }

        @Test
        @DisplayName("exchange: keycloak throws -> 401 invalid_code")
        void exchangeKeycloakThrows() {
            when(keycloakClient.exchangeCode(any(AuthExchangeRequest.class))).thenThrow(new RuntimeException("boom"));
            ResponseEntity<?> resp = controller.exchange(buildExchangeRequest());
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("invalid_code", body.get("error"));
        }
    }

    @Nested
    class RefreshTests {
        @Test
        @DisplayName("refresh: success")
        void refreshSuccess() {
            Map<String,Object> tokens = Map.of("access_token", "newAT", "refresh_token", "newRT", "expires_in", 300);
            when(keycloakClient.refresh("oldRT")).thenReturn(tokens);
            AuthController.RefreshRequest r = new AuthController.RefreshRequest();
            r.setRefreshToken("oldRT");
            ResponseEntity<?> resp = controller.refresh(r);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("newAT", body.get("access_token"));
        }

        @Test
        @DisplayName("refresh: missing refresh token -> 400")
        void refreshMissing() {
            AuthController.RefreshRequest r = new AuthController.RefreshRequest();
            ResponseEntity<?> resp = controller.refresh(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("missing_refresh_token", body.get("error"));
        }

        @Test
        @DisplayName("refresh: keycloak throws -> 401")
        void refreshThrows() {
            when(keycloakClient.refresh("bad")).thenThrow(new RuntimeException("bad"));
            AuthController.RefreshRequest r = new AuthController.RefreshRequest();
            r.setRefreshToken("bad");
            ResponseEntity<?> resp = controller.refresh(r);
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("invalid_refresh_token", body.get("error"));
        }
    }

    @Nested
    class MeTests {
        @Test
        @DisplayName("me: success returns claims + roles")
        void meSuccess() {
            Jwt jwt = Jwt.withTokenValue("tok")
                    .header("alg", "none")
                    .claim("sub", "user1")
                    .claim("preferred_username", "user1")
                    .claim("email", "u1@example.com")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build();
            var auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_user")));
            ResponseEntity<?> resp = controller.me(auth);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("user1", body.get("preferred_username"));
            List<?> roles = (List<?>) body.get("roles");
            assertTrue(roles.contains("user"));
        }

        @Test
        @DisplayName("me: missing auth -> 401")
        void meMissing() {
            ResponseEntity<?> resp = controller.me(null);
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        }
    }

    @Nested
    class LogoutTests {
        @Test
        @DisplayName("logout: success with refresh token")
        void logoutSuccess() {
            AuthController.LogoutRequest r = new AuthController.LogoutRequest();
            r.setRefreshToken("rt");
            ResponseEntity<?> resp = controller.logout(r);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals(true, body.get("success"));
            verify(keycloakClient, times(1)).logout(eq("rt"), isNull());
        }

        @Test
        @DisplayName("logout: missing tokens -> 400")
        void logoutMissing() {
            AuthController.LogoutRequest r = new AuthController.LogoutRequest();
            ResponseEntity<?> resp = controller.logout(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("missing_refresh_or_id_token", body.get("error"));
            verify(keycloakClient, never()).logout(any(), any());
        }
    }
}
