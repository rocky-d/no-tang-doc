package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.role.BatchRoleAssignRequest;
import com.ntdoc.notangdoccore.dto.role.UserRoleResponse;
import com.ntdoc.notangdoccore.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * 用户角色管理控制器
 * 管理Keycloak Realm级别的角色
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
@RequiredArgsConstructor
@Tag(name = "用户角色管理", description = "管理系统级别的用户角色 (USER/ADMIN)")
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * 为用户分配角色
     */
    @PostMapping("/{roleName}")
    @Operation(summary = "分配角色", description = "只有系统管理员可以分配Realm角色")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "角色名称 (USER/ADMIN)", required = true)
            @PathVariable String roleName,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to assign role: userId={}, roleName={}", userId, roleName);

            String operatorKcId = jwt.getClaimAsString("sub");

            userRoleService.assignRealmRole(userId, roleName.toUpperCase(), operatorKcId);

            log.info("Role assigned successfully: userId={}, roleName={}", userId, roleName);

            return ResponseEntity.ok(ApiResponse.success("角色分配成功"));

        } catch (SecurityException e) {
            log.warn("Access denied for assigning role: userId={}, roleName={}", userId, roleName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for assigning role: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to assign role: userId={}, roleName={}", userId, roleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "分配角色失败: " + e.getMessage()));
        }
    }

    /**
     * 撤销用户的角色
     */
    @DeleteMapping("/{roleName}")
    @Operation(summary = "撤销角色", description = "只有系统管理员可以撤销Realm角色")
    public ResponseEntity<ApiResponse<Void>> withdrawRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "角色名称 (USER/ADMIN)", required = true)
            @PathVariable String roleName,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to withdraw role: userId={}, roleName={}", userId, roleName);

            String operatorKcId = jwt.getClaimAsString("sub");

            userRoleService.withdrawRealmRole(userId, roleName.toUpperCase(), operatorKcId);

            log.info("Role withdrawn successfully: userId={}, roleName={}", userId, roleName);

            return ResponseEntity.ok(ApiResponse.success("角色撤销成功"));

        } catch (SecurityException e) {
            log.warn("Access denied for withdrawing role: userId={}, roleName={}", userId, roleName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Invalid request for withdrawing role: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to withdraw role: userId={}, roleName={}", userId, roleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "撤销角色失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的所有角色
     */
    @GetMapping
    @Operation(summary = "查询用户角色", description = "查询用户的Realm角色和团队角色")
    public ResponseEntity<ApiResponse<UserRoleResponse>> getUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to get user roles: userId={}", userId);

            String operatorKcId = jwt.getClaimAsString("sub");

            UserRoleResponse response = userRoleService.getUserRealmRoles(userId, operatorKcId);

            log.info("Retrieved user roles successfully: userId={}", userId);

            return ResponseEntity.ok(ApiResponse.success("获取用户角色成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for getting user roles: userId={}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get user roles: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取用户角色失败: " + e.getMessage()));
        }
    }

    /**
     * 批量分配角色
     */
    @PostMapping("/batch")
    @Operation(summary = "批量分配角色", description = "批量为用户分配多个Realm角色")
    public ResponseEntity<ApiResponse<Void>> batchAssignRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Valid @RequestBody BatchRoleAssignRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to batch assign roles: userId={}, roles={}",
                    userId, request.getRoleNames());

            String operatorKcId = jwt.getClaimAsString("sub");

            userRoleService.assignMultipleRealmRoles(userId, request.getRoleNames(), operatorKcId);

            log.info("Roles assigned successfully: userId={}, roles={}", userId, request.getRoleNames());

            return ResponseEntity.ok(ApiResponse.success("批量分配角色成功"));

        } catch (SecurityException e) {
            log.warn("Access denied for batch assigning roles: userId={}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for batch assigning roles: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to batch assign roles: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "批量分配角色失败: " + e.getMessage()));
        }
    }
}

