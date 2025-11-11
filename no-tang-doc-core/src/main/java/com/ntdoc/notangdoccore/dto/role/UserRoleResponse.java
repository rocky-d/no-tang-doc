package com.ntdoc.notangdoccore.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户角色响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * Keycloak用户ID
     */
    private String kcUserId;

    /**
     * Realm角色列表
     */
    private List<String> realmRoles;

    /**
     * 团队角色列表
     */
    private List<TeamRoleInfo> teamRoles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamRoleInfo {
        private Long teamId;
        private String teamName;
        private String role;
    }
}

