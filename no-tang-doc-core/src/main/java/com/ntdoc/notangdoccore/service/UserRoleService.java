package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.dto.role.UserRoleResponse;

import java.util.List;

/**
 * 用户角色管理服务接口
 * 管理Keycloak Realm级别的角色分配
 */
public interface UserRoleService {

    /**
     * 为用户分配Realm角色
     *
     * @param userId 用户ID
     * @param roleName 角色名称 (USER/ADMIN)
     * @param operatorKcId 操作者Keycloak ID
     */
    void assignRealmRole(Long userId, String roleName, String operatorKcId);

    /**
     * 撤销用户的Realm角色
     *
     * @param userId 用户ID
     * @param roleName 角色名称
     * @param operatorKcId 操作者Keycloak ID
     */
    void withdrawRealmRole(Long userId, String roleName, String operatorKcId);

    /**
     * 获取用户的所有Realm角色
     *
     * @param userId 用户ID
     * @param operatorKcId 操作者Keycloak ID
     * @return 角色响应对象
     */
    UserRoleResponse getUserRealmRoles(Long userId, String operatorKcId);

    /**
     * 批量分配角色
     *
     * @param userId 用户ID
     * @param roleNames 角色列表
     * @param operatorKcId 操作者Keycloak ID
     */
    void assignMultipleRealmRoles(Long userId, List<String> roleNames, String operatorKcId);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param roleName 角色名称
     * @return 是否拥有该角色
     */
    boolean hasRealmRole(Long userId, String roleName);
}

