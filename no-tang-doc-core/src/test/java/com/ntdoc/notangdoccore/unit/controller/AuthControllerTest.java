package com.ntdoc.notangdoccore.unit.controller;

import com.ntdoc.notangdoccore.config.AuthProperties;
import com.ntdoc.notangdoccore.config.KeycloakClient;
import com.ntdoc.notangdoccore.controller.AuthController;
import com.ntdoc.notangdoccore.dto.keycloak.AuthExchangeRequest;
import org.junit.jupiter.api.*;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AuthController单元测试")
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
        @Order(1)
        @DisplayName("测试1：exchange: success")
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
        @Order(2)
        @DisplayName("测试2：exchange: redirect not allowed -> 400")
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
        @Order(3)
        @DisplayName("测试3：exchange: missing code or verifier -> 400")
        void exchangeMissingCode() {
            AuthExchangeRequest r = buildExchangeRequest();
            r.setCode(null);
            ResponseEntity<?> resp = controller.exchange(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("missing_code_or_verifier", body.get("error"));
        }

        @Test
        @Order(4)
        @DisplayName("测试4：exchange: keycloak throws -> 401 invalid_code")
        void exchangeKeycloakThrows() {
            when(keycloakClient.exchangeCode(any(AuthExchangeRequest.class))).thenThrow(new RuntimeException("boom"));
            ResponseEntity<?> resp = controller.exchange(buildExchangeRequest());
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("invalid_code", body.get("error"));
        }

        @Test
        @Order(5)
        @DisplayName("测试5：exchange: allowedRedirectUris is null -> 400")
        void exchangeNullRedirectList() {
            AuthProperties p = new AuthProperties();
            p.setAllowedRedirectUris(null);
            AuthController ctrl = new AuthController(keycloakClient, p);

            ResponseEntity<?> resp = ctrl.exchange(buildExchangeRequest());
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("redirect_not_allowed", body.get("error"));
            verify(keycloakClient, never()).exchangeCode(any());
        }

        @Test
        @Order(6)
        @DisplayName("测试6：exchange: redirectUri is null -> 400")
        void exchangeRedirectUriNull() {
            AuthExchangeRequest r = buildExchangeRequest();
            r.setRedirectUri(null);
            ResponseEntity<?> resp = controller.exchange(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("redirect_not_allowed", body.get("error"));
            verify(keycloakClient, never()).exchangeCode(any());
        }

        @Test
        @Order(7)
        @DisplayName("测试7：exchange: codeVerifier is null -> 400")
        void exchangeMissingVerifier() {
            AuthExchangeRequest r = buildExchangeRequest();
            r.setCodeVerifier(null);
            ResponseEntity<?> resp = controller.exchange(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("missing_code_or_verifier", body.get("error"));
        }
    }

    @Nested
    class RefreshTests {
        @Test
        @Order(10)
        @DisplayName("测试10：refresh: success")
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
        @Order(11)
        @DisplayName("测试11：refresh: missing refresh token -> 400")
        void refreshMissing() {
            AuthController.RefreshRequest r = new AuthController.RefreshRequest();
            ResponseEntity<?> resp = controller.refresh(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("missing_refresh_token", body.get("error"));
        }

        @Test
        @Order(12)
        @DisplayName("测试12：refresh: keycloak throws -> 401")
        void refreshThrows() {
            when(keycloakClient.refresh("bad")).thenThrow(new RuntimeException("bad"));
            AuthController.RefreshRequest r = new AuthController.RefreshRequest();
            r.setRefreshToken("bad");
            ResponseEntity<?> resp = controller.refresh(r);
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("invalid_refresh_token", body.get("error"));
        }

        @Test
        @Order(13)
        @DisplayName("测试13：refresh: body null -> 400 missing_refresh_token")
        void refreshBodyNull() {
            ResponseEntity<?> resp = controller.refresh(null);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("missing_refresh_token", body.get("error"));
        }

        @Test
        @Order(14)
        @DisplayName("测试14：refresh: blank refresh token -> 400")
        void refreshBlankToken() {
            AuthController.RefreshRequest r = new AuthController.RefreshRequest();
            r.setRefreshToken("   "); // 空白
            ResponseEntity<?> resp = controller.refresh(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("missing_refresh_token", body.get("error"));
        }
    }

    @Nested
    class MeTests {
        @Test
        @Order(20)
        @DisplayName("测试20：me: success returns claims + roles")
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
        @Order(21)
        @DisplayName("测试21：me: missing auth -> 401")
        void meMissing() {
            ResponseEntity<?> resp = controller.me(null);
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        }

        @Test
        @Order(22)
        @DisplayName("测试22：me: jwt exists but not authenticated -> 401")
        void meNotAuthenticated() {
            Jwt jwt = Jwt.withTokenValue("tok")
                    .header("alg", "none")
                    .claim("sub", "user1")
                    .claim("email", "user1@example.com")
                    .build();

            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(), "user1");
            auth.setAuthenticated(false); // ✅ 手动设为未认证状态

            ResponseEntity<?> resp = controller.me(auth);
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("missing_token", body.get("error"));
        }

        @Test
        @Order(23)
        @DisplayName("测试23：me: missing preferred_username and name -> fallback to sub")
        void meMissingNameFields() {
            Jwt jwt = Jwt.withTokenValue("tok")
                    .header("alg", "none")
                    .claim("sub", "abc123")
                    .claim("email", "a@b.com")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build();
            var auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_admin")));
            ResponseEntity<?> resp = controller.me(auth);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("abc123", body.get("preferred_username"));
            assertEquals("abc123", body.get("name"));
        }
    }

    @Nested
    class LogoutTests {
        @Test
        @Order(30)
        @DisplayName("测试30：logout: success with refresh token")
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
        @Order(31)
        @DisplayName("测试31：logout: missing tokens -> 400")
        void logoutMissing() {
            AuthController.LogoutRequest r = new AuthController.LogoutRequest();
            ResponseEntity<?> resp = controller.logout(r);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?,?> body = (Map<?,?>) resp.getBody();
            assertEquals("missing_refresh_or_id_token", body.get("error"));
            verify(keycloakClient, never()).logout(any(), any());
        }

        @Test
        @Order(32)
        @DisplayName("测试32：logout: keycloak throws -> 500 logout_failed")
        void logoutKeycloakThrows() {
            AuthController.LogoutRequest r = new AuthController.LogoutRequest();
            r.setRefreshToken("rt");
            doThrow(new RuntimeException("fail")).when(keycloakClient).logout(eq("rt"), isNull());

            ResponseEntity<?> resp = controller.logout(r);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("logout_failed", body.get("error"));
        }

        @Test
        @Order(33)
        @DisplayName("测试33：logout: success with id token")
        void logoutWithIdToken() {
            AuthController.LogoutRequest r = new AuthController.LogoutRequest();
            r.setIdToken("id123");
            ResponseEntity<?> resp = controller.logout(r);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals(true, body.get("success"));
            verify(keycloakClient, times(1)).logout(isNull(), eq("id123"));
        }

        @Test
        @Order(34)
        @DisplayName("测试34：logout: null body -> 400")
        void logoutNullBody() {
            ResponseEntity<?> resp = controller.logout(null);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("missing_refresh_or_id_token", body.get("error"));
        }

        @Test
        @Order(35)
        @DisplayName("测试35：logout: both refresh and id token")
        void logoutBothTokens() {
            AuthController.LogoutRequest r = new AuthController.LogoutRequest();
            r.setRefreshToken("rt123");
            r.setIdToken("id123");
            ResponseEntity<?> resp = controller.logout(r);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            verify(keycloakClient).logout(eq("rt123"), eq("id123"));
        }
    }
}
