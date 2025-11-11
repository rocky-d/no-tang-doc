package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.role.UserRoleResponse;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.keycloak.KeycloakAdminService;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final KeycloakAdminService keycloakAdminService;

    @Override
    public void assignRealmRole(Long userId, String roleName, String operatorKcId) {
        log.info("Assigning realm role: userId={}, roleName={}, operator={}",
                userId, roleName, operatorKcId);

        // 1. 验证操作者是系统管理员
        if (!isSystemAdmin(operatorKcId)) {
            throw new SecurityException("只有系统管理员可以分配Realm角色");
        }

        // 2. 获取目标用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 3. 验证角色名称
        validateRoleName(roleName);

        // 4. 调用Keycloak API分配角色
        try {
            keycloakAdminService.assignRealmRole(user.getKcUserId(), roleName);
            log.info("Realm role assigned successfully: userId={}, roleName={}", userId, roleName);
        } catch (Exception e) {
            log.error("Failed to assign realm role: userId={}, roleName={}", userId, roleName, e);
            throw new RuntimeException("分配角色失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void withdrawRealmRole(Long userId, String roleName, String operatorKcId) {
        log.info("Withdrawing realm role: userId={}, roleName={}, operator={}",
                userId, roleName, operatorKcId);

        // 1. 验证操作者是系统管理员
        if (!isSystemAdmin(operatorKcId)) {
            throw new SecurityException("只有系统管理员可以撤销Realm角色");
        }

        // 2. 获取目标用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 3. 验证角色名称
        validateRoleName(roleName);

        // 4. 防止撤销最后一个管理员的ADMIN角色
        if ("ADMIN".equals(roleName)) {
            List<String> userRoles = keycloakAdminService.getUserRealmRoles(user.getKcUserId());
            if (userRoles.contains("ADMIN") && isLastAdmin()) {
                throw new IllegalStateException("不能撤销最后一个系统管理员的ADMIN角色");
            }
        }

        // 5. 调用Keycloak API撤销角色
        try {
            keycloakAdminService.removeRealmRole(user.getKcUserId(), roleName);
            log.info("Realm role withdrawn successfully: userId={}, roleName={}", userId, roleName);
        } catch (Exception e) {
            log.error("Failed to withdraw realm role: userId={}, roleName={}", userId, roleName, e);
            throw new RuntimeException("撤销角色失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleResponse getUserRealmRoles(Long userId, String operatorKcId) {
        log.debug("Getting user realm roles: userId={}, operator={}", userId, operatorKcId);

        // 1. 获取操作者
        User operator = userRepository.findByKcUserId(operatorKcId)
                .orElseThrow(() -> new RuntimeException("操作者不存在: " + operatorKcId));

        // 2. 权限验证：只能查看自己的角色，或者系统管理员可以查看所有人
        if (!operator.getId().equals(userId) && !isSystemAdmin(operatorKcId)) {
            throw new SecurityException("只能查看自己的角色信息");
        }

        // 3. 获取目标用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 4. 获取Realm角色
        List<String> realmRoles = keycloakAdminService.getUserRealmRoles(user.getKcUserId());

        // 5. 获取团队角色
        List<TeamMember> teamMembers = teamMemberRepository.findByUserAndStatus(
                user, TeamMember.MemberStatus.ACTIVE);

        List<UserRoleResponse.TeamRoleInfo> teamRoles = teamMembers.stream()
                .map(tm -> UserRoleResponse.TeamRoleInfo.builder()
                        .teamId(tm.getTeam().getId())
                        .teamName(tm.getTeam().getName())
                        .role(tm.getRole().name())
                        .build())
                .collect(Collectors.toList());

        // 6. 构建响应
        return UserRoleResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .kcUserId(user.getKcUserId())
                .realmRoles(realmRoles)
                .teamRoles(teamRoles)
                .build();
    }

    @Override
    public void assignMultipleRealmRoles(Long userId, List<String> roleNames, String operatorKcId) {
        log.info("Assigning multiple realm roles: userId={}, roles={}, operator={}",
                userId, roleNames, operatorKcId);

        // 验证操作者权限
        if (!isSystemAdmin(operatorKcId)) {
            throw new SecurityException("只有系统管理员可以分配Realm角色");
        }

        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 批量分配
        for (String roleName : roleNames) {
            try {
                validateRoleName(roleName);
                keycloakAdminService.assignRealmRole(user.getKcUserId(), roleName);
                log.info("Assigned role {} to user {}", roleName, userId);
            } catch (Exception e) {
                log.error("Failed to assign role {} to user {}: {}", roleName, userId, e.getMessage());
                // 继续处理其他角色
            }
        }

        log.info("Multiple realm roles assigned successfully: userId={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRealmRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        return keycloakAdminService.hasRealmRole(user.getKcUserId(), roleName);
    }

    /**
     * 检查用户是否是系统管理员
     */
    private boolean isSystemAdmin(String userKcId) {
        try {
            List<String> roles = keycloakAdminService.getUserRealmRoles(userKcId);
            return roles.contains("ADMIN");
        } catch (Exception e) {
            log.error("Failed to check admin role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证角色名称是否有效
     */
    private void validateRoleName(String roleName) {
        if (!"USER".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new IllegalArgumentException("无效的角色名称: " + roleName + "，只能是USER或ADMIN");
        }
    }

    /**
     * 检查是否是最后一个管理员
     */
    private boolean isLastAdmin() {
        // 获取所有用户
        List<User> allUsers = userRepository.findAll();

        // 统计拥有ADMIN角色的用户数量
        long adminCount = allUsers.stream()
                .filter(user -> {
                    try {
                        return keycloakAdminService.hasRealmRole(user.getKcUserId(), "ADMIN");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        return adminCount <= 1;
    }
}

