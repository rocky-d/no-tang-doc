package com.ntdoc.notangdoccore.dto.team;

import com.ntdoc.notangdoccore.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队列表响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队列表响应")
public class TeamListResponse {

    @Schema(description = "团队列表")
    private List<TeamResponse> teams;

    @Schema(description = "团队总数", example = "5")
    private Integer total;

    /**
     * 从 Team 实体列表转换为 DTO
     */
    public static TeamListResponse fromEntities(List<Team> teams) {
        List<TeamResponse> teamResponses = teams.stream()
                .map(TeamResponse::fromEntity)
                .collect(Collectors.toList());

        return TeamListResponse.builder()
                .teams(teamResponses)
                .total(teamResponses.size())
                .build();
    }
}

