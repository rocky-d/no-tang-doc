package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.keycloak.KeycloakAdminService;
import com.ntdoc.notangdoccore.dto.keycloak.RegistrationRequest;

import com.ntdoc.notangdoccore.service.UserSyncService;
import com.ntdoc.notangdoccore.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SampleController {
    private final KeycloakAdminService keycloakAdminService;
    private final UserSyncService userSyncService;
    private final WeatherService weatherService;


    @PostMapping("/public/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest req) {
        String userId = keycloakAdminService.createUser(
                req.getUsername(),
                req.getEmail(),
                req.getPassword(),
                req.getRoles() == null || req.getRoles().isEmpty() ? List.of("USER") : req.getRoles()
        );
        return ResponseEntity.created(URI.create("/api/admin/users/" + userId)).body(Map.of("keycloakUserId", userId));
    }

    @GetMapping("/user/me")
    public Map<String, Object> me(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        userSyncService.ensureLocalUser(username, email);
        return Map.of(
                "username", username,
                "email", email,
                "roles", jwt.getClaim("realm_access")
        );
    }


    @GetMapping(value = "/user/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String userHello(@RequestParam String city, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("preferred_username");
        String weather = weatherService.getWeather(city);
        return "Hello, " + username + ", " + city + "今日天气：" + weather;
    }
}
