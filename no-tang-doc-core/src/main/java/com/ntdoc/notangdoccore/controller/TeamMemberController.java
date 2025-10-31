package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.team.TeamMemberAddRequest;
import com.ntdoc.notangdoccore.dto.team.TeamMemberListResponse;
import com.ntdoc.notangdoccore.dto.team.TeamMemberResponse;
import com.ntdoc.notangdoccore.dto.team.TeamMemberUpdateRequest;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.service.TeamMemberService;
import com.ntdoc.notangdoccore.service.TeamService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/teams/{teamId}/members")
@RequiredArgsConstructor
@Tag(name = "团队成员管理", description = "团队成员的添加、移除、角色管理等操作")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;
    private final TeamService teamService;

    /**
     * 添加成员到团队
     */
    @PostMapping
    @Operation(summary = "添加成员到团队", description = "只有团队拥有者或管理员可以添加成员")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addMember(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @Valid @RequestBody TeamMemberAddRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to add member: teamId={}, userKcId={}",
                    teamId, request.getUserKcId());

            String operatorKcId = jwt.getClaimAsString("sub");

            TeamMember member = teamMemberService.addMember(
                    teamId,
                    request.getUserKcId(),
                    request.getRole(),
                    operatorKcId
            );

            TeamMemberResponse response = TeamMemberResponse.fromEntity(member);

            log.info("Member added successfully: teamId={}, memberId={}", teamId, member.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("成员添加成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for adding member: teamId={}", teamId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Invalid request for adding member: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to add member: teamId={}", teamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加成员失败: " + e.getMessage()));
        }
    }

    /**
     * 获取团队成员列表
     */
    @GetMapping
    @Operation(summary = "获取团队成员列表", description = "获取团队的所有活跃成员")
    public ResponseEntity<ApiResponse<TeamMemberListResponse>> getTeamMembers(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @Parameter(description = "是否只获取活跃成员")
            @RequestParam(value = "activeOnly", required = false, defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to get team members: teamId={}, activeOnly={}",
                    teamId, activeOnly);

            String operatorKcId = jwt.getClaimAsString("sub");

            List<TeamMember> members = activeOnly
                    ? teamMemberService.getActiveTeamMembers(teamId, operatorKcId)
                    : teamMemberService.getTeamMembers(teamId, operatorKcId);

            Team team = teamService.getTeamById(teamId);
            TeamMemberListResponse response = TeamMemberListResponse.fromEntities(members, team.getName());

            log.info("Retrieved {} members for team: teamId={}", members.size(), teamId);

            return ResponseEntity.ok(ApiResponse.success("获取成员列表成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for getting members: teamId={}", teamId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get team members: teamId={}", teamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取成员列表失败: " + e.getMessage()));
        }
    }

    /**
     * 更新成员角色
     */
    @PutMapping("/{memberId}")
    @Operation(summary = "更新成员角色", description = "只有团队拥有者可以修改成员角色")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> updateMemberRole(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @Parameter(description = "成员记录ID", required = true)
            @PathVariable Long memberId,
            @Valid @RequestBody TeamMemberUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to update member role: teamId={}, memberId={}, newRole={}",
                    teamId, memberId, request.getRole());

            String operatorKcId = jwt.getClaimAsString("sub");

            TeamMember member = teamMemberService.updateMemberRole(
                    teamId,
                    memberId,
                    request.getRole(),
                    operatorKcId
            );

            TeamMemberResponse response = TeamMemberResponse.fromEntity(member);

            log.info("Member role updated successfully: teamId={}, memberId={}", teamId, memberId);

            return ResponseEntity.ok(ApiResponse.success("成员角色更新成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for updating member role: teamId={}, memberId={}", teamId, memberId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for updating member role: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update member role: teamId={}, memberId={}", teamId, memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新成员角色失败: " + e.getMessage()));
        }
    }

    /**
     * 移除团队成员
     */
    @DeleteMapping("/{memberId}")
    @Operation(summary = "移除团队成员", description = "只有团队拥有者或管理员可以移除成员")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @Parameter(description = "成员记录ID", required = true)
            @PathVariable Long memberId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to remove member: teamId={}, memberId={}", teamId, memberId);

            String operatorKcId = jwt.getClaimAsString("sub");

            teamMemberService.removeMember(teamId, memberId, operatorKcId);

            log.info("Member removed successfully: teamId={}, memberId={}", teamId, memberId);

            return ResponseEntity.ok(ApiResponse.success("成员移除成功"));

        } catch (SecurityException e) {
            log.warn("Access denied for removing member: teamId={}, memberId={}", teamId, memberId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for removing member: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to remove member: teamId={}, memberId={}", teamId, memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "移除成员失败: " + e.getMessage()));
        }
    }

    /**
     * 退出团队（成员自己退出）
     */
    @PostMapping("/leave")
    @Operation(summary = "退出团队", description = "成员主动退出团队（拥有者不能退出）")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to leave team: teamId={}", teamId);

            String userKcId = jwt.getClaimAsString("sub");

            teamMemberService.leaveTeam(teamId, userKcId);

            log.info("User left team successfully: teamId={}", teamId);

            return ResponseEntity.ok(ApiResponse.success("成功退出团队"));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for leaving team: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to leave team: teamId={}", teamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "退出团队失败: " + e.getMessage()));
        }
    }
}

