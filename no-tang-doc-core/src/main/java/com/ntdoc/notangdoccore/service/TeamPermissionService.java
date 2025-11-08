package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.TeamMember;

/**
 * 团队权限服务
 * 用于统一管理和验证团队相关的权限逻辑
 */
public interface TeamPermissionService {

    /**
     * 检查用户是否有管理团队成员的权限
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否有管理权限（OWNER 或 ADMIN）
     */
    boolean hasManageMemberPermission(Long teamId, String userKcId);

    /**
     * 检查用户是否有修改团队信息的权限
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否有编辑权限（仅 OWNER）
     */
    boolean hasEditTeamPermission(Long teamId, String userKcId);

    /**
     * 检查用户是否有删除团队的权限
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否有删除权限（仅 OWNER）
     */
    boolean hasDeleteTeamPermission(Long teamId, String userKcId);

    /**
     * 检查用户是否有邀请成员的权限
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否有邀请权限（OWNER 或 ADMIN）
     */
    boolean hasInviteMemberPermission(Long teamId, String userKcId);

    /**
     * 检查用户是否有修改角色的权限
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 是否有修改角色权限（仅 OWNER）
     */
    boolean hasModifyRolePermission(Long teamId, String userKcId);

    /**
     * 检查用户是否可以移除指定成员
     *
     * @param teamId 团队ID
     * @param targetMemberId 目标成员ID
     * @param operatorKcId 操作者Keycloak ID
     * @return 是否可以移除
     */
    boolean canRemoveMember(Long teamId, Long targetMemberId, String operatorKcId);

    /**
     * 获取用户在团队中的角色
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @return 角色枚举，如果不是成员则返回null
     */
    TeamMember.TeamRole getUserRole(Long teamId, String userKcId);

    /**
     * 验证用户是否至少有指定的角色级别
     *
     * @param teamId 团队ID
     * @param userKcId 用户Keycloak ID
     * @param requiredRole 要求的最低角色
     * @return 是否满足角色要求
     */
    boolean hasMinimumRole(Long teamId, String userKcId, TeamMember.TeamRole requiredRole);

    /**
     * 验证角色变更的合法性
     *
     * @param operatorRole 操作者的角色
     * @param targetRole 目标用户当前角色
     * @param newRole 要设置的新角色
     * @return 是否允许变更
     */
    boolean isRoleChangeAllowed(TeamMember.TeamRole operatorRole,
                                TeamMember.TeamRole targetRole,
                                TeamMember.TeamRole newRole);
}

