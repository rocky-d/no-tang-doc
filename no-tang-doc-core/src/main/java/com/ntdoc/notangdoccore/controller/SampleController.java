package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.keycloak.KeycloakAdminService;
import com.ntdoc.notangdoccore.dto.keycloak.RegistrationRequest;

import com.ntdoc.notangdoccore.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "示例接口", description = "示例控制器")
public class SampleController {
    private final KeycloakAdminService keycloakAdminService;
    private final UserSyncService userSyncService;



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
        var user = userSyncService.ensureFromJwt(jwt);

        return Map.of(
                "kcUserId",user.getKcUserId(),
                "username", username,
                "email", email,
                "roles", jwt.getClaim("realm_access")
        );
    }


    @GetMapping("/sample")
    @Operation(summary = "获取示例数据", description = "返回示例数据")
    @ApiResponse(responseCode = "200", description = "成功")
    public String getSample() {
        return "Hello from Sample Controller";
    }
}
