package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.dto.team.TeamCreateRequest;
import com.ntdoc.notangdoccore.entity.Team;

import java.util.List;

/**
 * 团队业务服务接口
 */
public interface TeamService {

    /**
     * 创建团队
     *
     * @param request 团队创建请求
     * @param kcUserId Keycloak 用户ID
     * @return 创建的团队
     */
    Team createTeam(TeamCreateRequest request, String kcUserId);

    /**
     * 根据ID获取团队
     *
     * @param teamId 团队ID
     * @return 团队实体
     */
    Team getTeamById(Long teamId);

    /**
     * 获取用户拥有的所有团队
     *
     * @param kcUserId Keycloak 用户ID
     * @return 团队列表
     */
    List<Team> getUserOwnedTeams(String kcUserId);

    /**
     * 获取用户拥有的活跃团队
     *
     * @param kcUserId Keycloak 用户ID
     * @return 团队列表
     */
    List<Team> getUserActiveTeams(String kcUserId);

    /**
     * 更新团队信息
     *
     * @param teamId 团队ID
     * @param request 更新请求
     * @param kcUserId Keycloak 用户ID（用于权限验证）
     * @return 更新后的团队
     */
    Team updateTeam(Long teamId, TeamCreateRequest request, String kcUserId);

    /**
     * 删除团队（软删除）
     *
     * @param teamId 团队ID
     * @param kcUserId Keycloak 用户ID（用于权限验证）
     */
    void deleteTeam(Long teamId, String kcUserId);

    /**
     * 验证用户是否是团队拥有者
     *
     * @param teamId 团队ID
     * @param kcUserId Keycloak 用户ID
     * @return 是否是拥有者
     */
    boolean isTeamOwner(Long teamId, String kcUserId);
}

