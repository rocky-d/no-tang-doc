package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.team.TeamCreateRequest;
import com.ntdoc.notangdoccore.dto.team.TeamListResponse;
import com.ntdoc.notangdoccore.dto.team.TeamResponse;
import com.ntdoc.notangdoccore.entity.Team;
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
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "团队管理", description = "团队创建、查询、更新、删除等操作")
public class TeamController {

    private final TeamService teamService;

    /**
     * 创建团队
     */
    @PostMapping
    @Operation(summary = "创建团队", description = "创建一个新的团队，创建者自动成为团队拥有者")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received team creation request: name={}", request.getName());

            String kcUserId = jwt.getClaimAsString("sub");

            Team team = teamService.createTeam(request, kcUserId);
            TeamResponse response = TeamResponse.fromEntity(team);

            log.info("Team created successfully: teamId={}, name={}", team.getId(), team.getName());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("团队创建成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid team creation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create team", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "团队创建失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的所有团队
     */
    @GetMapping
    @Operation(summary = "获取用户的所有团队", description = "获取当前用户拥有的所有团队列表")
    public ResponseEntity<ApiResponse<TeamListResponse>> getUserTeams(
            @Parameter(description = "是否只获取活跃团队")
            @RequestParam(value = "activeOnly", required = false, defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to get user teams: activeOnly={}", activeOnly);

            String kcUserId = jwt.getClaimAsString("sub");

            List<Team> teams = activeOnly
                    ? teamService.getUserActiveTeams(kcUserId)
                    : teamService.getUserOwnedTeams(kcUserId);

            TeamListResponse response = TeamListResponse.fromEntities(teams);

            log.info("Retrieved {} teams for user", teams.size());

            return ResponseEntity.ok(ApiResponse.success("获取团队列表成功", response));

        } catch (Exception e) {
            log.error("Failed to get user teams", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取团队列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取团队详情
     */
    @GetMapping("/{teamId}")
    @Operation(summary = "获取团队详情", description = "根据团队ID获取团队的详细信息")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to get team: teamId={}", teamId);

            Team team = teamService.getTeamById(teamId);
            TeamResponse response = TeamResponse.fromEntity(team);

            log.info("Retrieved team successfully: teamId={}", teamId);

            return ResponseEntity.ok(ApiResponse.success("获取团队信息成功", response));

        } catch (RuntimeException e) {
            log.warn("Team not found: teamId={},{}", teamId,e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "团队不存在"));
        } catch (Exception e) {
            log.error("Failed to get team: teamId={}", teamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取团队信息失败: " + e.getMessage()));
        }
    }

    /**
     * 更新团队信息
     */
    @PutMapping("/{teamId}")
    @Operation(summary = "更新团队信息", description = "更新团队的名称和描述（仅团队拥有者）")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to update team: teamId={}, name={}", teamId, request.getName());

            String kcUserId = jwt.getClaimAsString("sub");

            Team team = teamService.updateTeam(teamId, request, kcUserId);
            TeamResponse response = TeamResponse.fromEntity(team);

            log.info("Team updated successfully: teamId={}", teamId);

            return ResponseEntity.ok(ApiResponse.success("团队更新成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for team update: teamId={}", teamId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid team update request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update team: teamId={}", teamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "团队更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除团队
     */
    @DeleteMapping("/{teamId}")
    @Operation(summary = "删除团队", description = "删除团队（软删除，仅团队拥有者）")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @Parameter(description = "团队ID", required = true)
            @PathVariable Long teamId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to delete team: teamId={}", teamId);

            String kcUserId = jwt.getClaimAsString("sub");

            teamService.deleteTeam(teamId, kcUserId);

            log.info("Team deleted successfully: teamId={}", teamId);

            return ResponseEntity.ok(ApiResponse.success("团队删除成功"));

        } catch (SecurityException e) {
            log.warn("Access denied for team deletion: teamId={}", teamId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Team not found: teamId={}", teamId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "团队不存在"));
        } catch (Exception e) {
            log.error("Failed to delete team: teamId={}", teamId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "团队删除失败: " + e.getMessage()));
        }
    }
}

