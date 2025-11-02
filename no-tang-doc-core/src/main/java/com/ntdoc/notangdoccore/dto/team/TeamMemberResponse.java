package com.ntdoc.notangdoccore.dto.team;

import com.ntdoc.notangdoccore.entity.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 团队成员响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队成员响应")
public class TeamMemberResponse {

    @Schema(description = "成员记录ID", example = "1")
    private Long id;

    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    @Schema(description = "团队名称", example = "开发团队")
    private String teamName;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户Keycloak ID", example = "user-kc-id-123")
    private String userKcId;

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "用户邮箱", example = "john@example.com")
    private String email;

    @Schema(description = "团队角色", example = "MEMBER")
    private String role;

    @Schema(description = "成员状态", example = "ACTIVE")
    private String status;

    @Schema(description = "加入时间")
    private Instant joinedAt;

    @Schema(description = "更新时间")
    private Instant updatedAt;

    /**
     * 从 TeamMember 实体转换为 DTO
     */
    public static TeamMemberResponse fromEntity(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .userId(member.getUser().getId())
                .userKcId(member.getUser().getKcUserId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .role(member.getRole().name())
                .status(member.getStatus().name())
                .joinedAt(member.getJoinedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}

