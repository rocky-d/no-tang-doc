package com.ntdoc.notangdoccore.dto.team;

import com.ntdoc.notangdoccore.entity.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队成员列表响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队成员列表响应")
public class TeamMemberListResponse {

    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    @Schema(description = "团队名称", example = "开发团队")
    private String teamName;

    @Schema(description = "成员总数", example = "5")
    private Integer totalMembers;

    @Schema(description = "成员列表")
    private List<TeamMemberResponse> members;

    /**
     * 从 TeamMember 实体列表转换为 DTO
     */
    public static TeamMemberListResponse fromEntities(List<TeamMember> members, String teamName) {
        List<TeamMemberResponse> memberResponses = members.stream()
                .map(TeamMemberResponse::fromEntity)
                .collect(Collectors.toList());

        return TeamMemberListResponse.builder()
                .teamId(members.isEmpty() ? null : members.get(0).getTeam().getId())
                .teamName(teamName)
                .totalMembers(memberResponses.size())
                .members(memberResponses)
                .build();
    }
}

