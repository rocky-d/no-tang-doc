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

    @Schema(description = "创建时间")
    private Instant createdAt;

    @Schema(description = "更新时间")
    private Instant updatedAt;

    /**
     * 从 Team 实体转换为 DTO
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
}

