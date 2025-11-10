package com.ntdoc.notangdoccore.dto.team;

import com.ntdoc.notangdoccore.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 团队响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队响应")
public class TeamResponse {

    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    @Schema(description = "团队名称", example = "开发团队")
    private String name;

    @Schema(description = "团队描述", example = "这是一个专注于后端开发的团队")
    private String description;

    @Schema(description = "拥有者ID", example = "1")
    private Long ownerId;

    @Schema(description = "拥有者用户名", example = "john_doe")
    private String ownerUsername;

    @Schema(description = "团队状态", example = "ACTIVE")
    private String status;

    @Schema(description = "成员数量", example = "1")
    private Integer memberCount;

    @Schema(description = "当前用户在该团队的角色", example = "OWNER", allowableValues = {"OWNER", "ADMIN", "MEMBER"})
    private String currentUserRole;

    @Schema(description = "当前用户的权限")
    private TeamPermissions permissions;

    @Schema(description = "创建时间")
    private Instant createdAt;

    @Schema(description = "更新时间")
    private Instant updatedAt;

    /**
     * 从 Team 实体转换为 DTO（不含角色信息）
     */
    public static TeamResponse fromEntity(Team team) {
        return TeamResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .ownerId(team.getOwner().getId())
                .ownerUsername(team.getOwner().getUsername())
                .status(team.getStatus().name())
                .memberCount(team.getMemberCount())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    /**
     * 从 Team 实体转换为 DTO（含角色信息）
     */
    public static TeamResponse fromEntityWithRole(Team team, String currentUserRole) {
        TeamResponse response = fromEntity(team);
        response.setCurrentUserRole(currentUserRole);
        response.setPermissions(TeamPermissions.fromRole(currentUserRole));
        return response;
    }

    /**
     * 团队权限DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "团队权限")
    public static class TeamPermissions {
        @Schema(description = "是否可以编辑团队信息", example = "true")
        private Boolean canEdit;

        @Schema(description = "是否可以邀请成员", example = "true")
        private Boolean canInvite;

        @Schema(description = "是否可以删除团队", example = "true")
        private Boolean canDelete;

        @Schema(description = "是否可以管理成员", example = "true")
        private Boolean canManageMembers;

        @Schema(description = "是否可以修改成员角色", example = "true")
        private Boolean canModifyRoles;

        /**
         * 根据角色生成权限
         */
        public static TeamPermissions fromRole(String role) {
            if (role == null) {
                return TeamPermissions.builder()
                        .canEdit(false)
                        .canInvite(false)
                        .canDelete(false)
                        .canManageMembers(false)
                        .canModifyRoles(false)
                        .build();
            }

            switch (role) {
                case "OWNER":
                    return TeamPermissions.builder()
                            .canEdit(true)
                            .canInvite(true)
                            .canDelete(true)
                            .canManageMembers(true)
                            .canModifyRoles(true)
                            .build();
                case "ADMIN":
                    return TeamPermissions.builder()
                            .canEdit(true)
                            .canInvite(true)
                            .canDelete(false)
                            .canManageMembers(true)
                            .canModifyRoles(false)
                            .build();
                case "MEMBER":
                    return TeamPermissions.builder()
                            .canEdit(false)
                            .canInvite(false)
                            .canDelete(false)
                            .canManageMembers(false)
                            .canModifyRoles(false)
                            .build();
                default:
                    return TeamPermissions.builder()
                            .canEdit(false)
                            .canInvite(false)
                            .canDelete(false)
                            .canManageMembers(false)
                            .canModifyRoles(false)
                            .build();
            }
        }
    }
}
