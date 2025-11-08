package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.TeamPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 团队权限服务实现
 * 提供统一的权限验证逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamPermissionServiceImpl implements TeamPermissionService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    public boolean hasManageMemberPermission(Long teamId, String userKcId) {
        TeamMember.TeamRole role = getUserRole(teamId, userKcId);
        if (role == null) {
            log.debug("User {} is not a member of team {}", userKcId, teamId);
            return false;
        }

        boolean hasPermission = role == TeamMember.TeamRole.OWNER ||
                               role == TeamMember.TeamRole.ADMIN;

        log.debug("User {} manage permission in team {}: {}", userKcId, teamId, hasPermission);
        return hasPermission;
    }

    @Override
    public boolean hasEditTeamPermission(Long teamId, String userKcId) {
        TeamMember.TeamRole role = getUserRole(teamId, userKcId);
        boolean hasPermission = role == TeamMember.TeamRole.OWNER;

        log.debug("User {} edit team permission in team {}: {}", userKcId, teamId, hasPermission);
        return hasPermission;
    }

    @Override
    public boolean hasDeleteTeamPermission(Long teamId, String userKcId) {
        // 只有 OWNER 可以删除团队
        return hasEditTeamPermission(teamId, userKcId);
    }

    @Override
    public boolean hasInviteMemberPermission(Long teamId, String userKcId) {
        // OWNER 和 ADMIN 都可以邀请成员
        return hasManageMemberPermission(teamId, userKcId);
    }

    @Override
    public boolean hasModifyRolePermission(Long teamId, String userKcId) {
        // 只有 OWNER 可以修改角色
        TeamMember.TeamRole role = getUserRole(teamId, userKcId);
        boolean hasPermission = role == TeamMember.TeamRole.OWNER;

        log.debug("User {} modify role permission in team {}: {}", userKcId, teamId, hasPermission);
        return hasPermission;
    }

    @Override
    public boolean canRemoveMember(Long teamId, Long targetMemberId, String operatorKcId) {
        // 1. 操作者必须有管理权限
        if (!hasManageMemberPermission(teamId, operatorKcId)) {
            log.warn("Operator {} does not have manage permission in team {}", operatorKcId, teamId);
            return false;
        }

        // 2. 获取目标成员
        TeamMember targetMember = teamMemberRepository.findById(targetMemberId).orElse(null);
        if (targetMember == null) {
            log.warn("Target member {} not found", targetMemberId);
            return false;
        }

        // 3. 不能移除 OWNER
        if (targetMember.getRole() == TeamMember.TeamRole.OWNER) {
            log.warn("Cannot remove OWNER from team");
            return false;
        }

        // 4. ADMIN 只能移除 MEMBER，不能移除其他 ADMIN
        TeamMember.TeamRole operatorRole = getUserRole(teamId, operatorKcId);
        if (operatorRole == TeamMember.TeamRole.ADMIN) {
            if (targetMember.getRole() == TeamMember.TeamRole.ADMIN) {
                log.warn("ADMIN cannot remove another ADMIN");
                return false;
            }
        }

        return true;
    }

    @Override
    public TeamMember.TeamRole getUserRole(Long teamId, String userKcId) {
        try {
            Team team = teamRepository.findById(teamId).orElse(null);
            if (team == null) {
                log.debug("Team {} not found", teamId);
                return null;
            }

            User user = userRepository.findByKcUserId(userKcId).orElse(null);
            if (user == null) {
                log.debug("User {} not found", userKcId);
                return null;
            }

            Optional<TeamMember> memberOpt = teamMemberRepository
                    .findByTeamAndUserAndStatus(team, user, TeamMember.MemberStatus.ACTIVE);

            if (memberOpt.isPresent()) {
                TeamMember.TeamRole role = memberOpt.get().getRole();
                log.debug("User {} has role {} in team {}", userKcId, role, teamId);
                return role;
            }

            log.debug("User {} is not a member of team {}", userKcId, teamId);
            return null;
        } catch (Exception e) {
            log.error("Error getting user role: teamId={}, userKcId={}", teamId, userKcId, e);
            return null;
        }
    }

    @Override
    public boolean hasMinimumRole(Long teamId, String userKcId, TeamMember.TeamRole requiredRole) {
        TeamMember.TeamRole userRole = getUserRole(teamId, userKcId);
        if (userRole == null) {
            return false;
        }

        // 角色级别：OWNER > ADMIN > MEMBER
        int userLevel = getRoleLevel(userRole);
        int requiredLevel = getRoleLevel(requiredRole);

        boolean hasMinRole = userLevel >= requiredLevel;
        log.debug("User {} minimum role check in team {}: has {}, requires {}, result: {}",
                userKcId, teamId, userRole, requiredRole, hasMinRole);

        return hasMinRole;
    }

    @Override
    public boolean isRoleChangeAllowed(TeamMember.TeamRole operatorRole,
                                      TeamMember.TeamRole targetRole,
                                      TeamMember.TeamRole newRole) {
        // 1. 只有 OWNER 可以修改角色
        if (operatorRole != TeamMember.TeamRole.OWNER) {
            log.warn("Only OWNER can modify roles, operator role: {}", operatorRole);
            return false;
        }

        // 2. 不能将角色改为 OWNER（防止多个 OWNER）
        if (newRole == TeamMember.TeamRole.OWNER) {
            log.warn("Cannot assign OWNER role through role change");
            return false;
        }

        // 3. 不能修改 OWNER 的角色
        if (targetRole == TeamMember.TeamRole.OWNER) {
            log.warn("Cannot change OWNER's role");
            return false;
        }

        log.debug("Role change allowed: operator={}, target={}, new={}",
                operatorRole, targetRole, newRole);
        return true;
    }

    /**
     * 获取角色级别（用于比较）
     * OWNER = 3, ADMIN = 2, MEMBER = 1
     */
    private int getRoleLevel(TeamMember.TeamRole role) {
        switch (role) {
            case OWNER:
                return 3;
            case ADMIN:
                return 2;
            case MEMBER:
                return 1;
            default:
                return 0;
        }
    }
}

