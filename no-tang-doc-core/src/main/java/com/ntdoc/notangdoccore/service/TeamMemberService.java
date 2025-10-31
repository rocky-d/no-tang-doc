package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.TeamMember;

import java.util.List;

/**
 * 团队成员管理服务接口
 */
public interface TeamMemberService {

    /**
     * 添加成员到团队
     *
     * @param teamId 团队ID
     * @param userKcId 要添加的用户Keycloak ID
     * @param role 角色
     * @param operatorKcId 操作者Keycloak ID（用于权限验证）
     * @return 添加的团队成员
     */
    TeamMember addMember(Long teamId, String userKcId, String role, String operatorKcId);

    /**
     * 移除团队成员
     *
     * @param teamId 团队ID
     * @param memberId 成员记录ID
     * @param operatorKcId 操作者Keycloak ID（用于权限验证）
     */
    void removeMember(Long teamId, Long memberId, String operatorKcId);

    /**
     * 更新成员角色
     *
     * @param teamId 团队ID
     * @param memberId 成员记录ID
     * @param newRole 新角色
     * @param operatorKcId 操作者Keycloak ID（用于权限验证）
     * @return 更新后的成员
     */
    TeamMember updateMemberRole(Long teamId, Long memberId, String newRole, String operatorKcId);

    /**
     * 获取团队所有成员
     *
     * @param teamId 团队ID
     * @param operatorKcId 操作者Keycloak ID（用于权限验证）
     * @return 成员列表
     */
    List<TeamMember> getTeamMembers(Long teamId, String operatorKcId);

    /**
     * 获取团队活跃成员
     *
     * @param teamId 团队ID
     * @param operatorKcId 操作者Keycloak ID（用于权限验证）
     * @return 活跃成员列表
     */
    List<TeamMember> getActiveTeamMembers(Long teamId, String operatorKcId);

    /**
     * 成员退出团队（自己退出）
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     */
    void leaveTeam(Long teamId, String userKcId);

    /**
     * 检查用户是否是团队成员
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否是成员
     */
    boolean isMember(Long teamId, String userKcId);

    /**
     * 检查用户是否有管理权限（OWNER 或 ADMIN）
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否有管理权限
     */
    boolean hasManagePermission(Long teamId, String userKcId);
}

