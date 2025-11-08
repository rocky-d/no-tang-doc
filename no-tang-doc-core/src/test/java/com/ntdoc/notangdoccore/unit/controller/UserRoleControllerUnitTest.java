package com.ntdoc.notangdoccore.unit.controller;

import com.ntdoc.notangdoccore.controller.UserRoleController;
import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.role.UserRoleResponse;
import com.ntdoc.notangdoccore.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserRoleController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleController 单元测试")
class UserRoleControllerUnitTest {

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private UserRoleController userRoleController;

    private Jwt adminJwt;
    private Jwt userJwt;

    @BeforeEach
    void setUp() {
        // 创建管理员 JWT
        Map<String, Object> adminHeaders = new HashMap<>();
        adminHeaders.put("alg", "RS256");

        Map<String, Object> adminClaims = new HashMap<>();
        adminClaims.put("sub", "admin-kc-id");
        adminClaims.put("preferred_username", "admin");
        adminClaims.put("email", "admin@example.com");

        adminJwt = new Jwt(
                "admin-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                adminHeaders,
                adminClaims
        );

        // 创建普通用户 JWT
        Map<String, Object> userHeaders = new HashMap<>();
        userHeaders.put("alg", "RS256");

        Map<String, Object> userClaims = new HashMap<>();
        userClaims.put("sub", "user-kc-id");
        userClaims.put("preferred_username", "user");
        userClaims.put("email", "user@example.com");

        userJwt = new Jwt(
                "user-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                userHeaders,
                userClaims
        );
    }

    // ========== 分配角色测试 ==========

    @Test
    @DisplayName("测试1: 分配角色 - 成功")
    void testAssignRole_Success() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";

        doNothing().when(userRoleService).assignRealmRole(eq(userId), eq("ADMIN"), eq("admin-kc-id"));

        // When
        ResponseEntity<ApiResponse<Void>> response = userRoleController.assignRole(userId, roleName, adminJwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("角色分配成功");
        assertThat(response.getBody().getCode()).isEqualTo(200);

        verify(userRoleService).assignRealmRole(eq(userId), eq("ADMIN"), eq("admin-kc-id"));
    }

    @Test
    @DisplayName("测试2: 分配角色 - 权限拒绝")
    void testAssignRole_Forbidden() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";

        doThrow(new SecurityException("只有系统管理员可以分配Realm角色"))
                .when(userRoleService).assignRealmRole(eq(userId), eq("ADMIN"), eq("user-kc-id"));

        // When
        ResponseEntity<ApiResponse<Void>> response = userRoleController.assignRole(userId, roleName, userJwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo(403);

        verify(userRoleService).assignRealmRole(eq(userId), eq("ADMIN"), eq("user-kc-id"));
    }

    @Test
    @DisplayName("测试3: 查询用户角色 - 成功")
    void testGetUserRoles_Success() {
        // Given
        Long userId = 1L;

        UserRoleResponse mockResponse = UserRoleResponse.builder()
                .userId(userId)
                .username("testuser")
                .email("test@example.com")
                .kcUserId("kc-user-123")
                .realmRoles(Arrays.asList("USER", "ADMIN"))
                .teamRoles(Collections.emptyList())
                .build();

        when(userRoleService.getUserRealmRoles(eq(userId), eq("admin-kc-id"))).thenReturn(mockResponse);

        // When
        ResponseEntity<ApiResponse<UserRoleResponse>> response = userRoleController.getUserRoles(userId, adminJwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");

        verify(userRoleService).getUserRealmRoles(eq(userId), eq("admin-kc-id"));
    }
}

