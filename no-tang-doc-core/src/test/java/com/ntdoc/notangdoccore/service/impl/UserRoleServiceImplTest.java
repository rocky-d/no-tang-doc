package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.role.UserRoleResponse;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.keycloak.KeycloakAdminService;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserRoleServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleService 单元测试")
class UserRoleServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .kcUserId("kc-user-123")
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .kcUserId("kc-admin-456")
                .build();
    }

    // ========== 分配角色测试 ==========

    @Test
    @DisplayName("测试1: 分配角色 - 成功")
    void testAssignRealmRole_Success() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(keycloakAdminService).assignRealmRole(testUser.getKcUserId(), roleName);

        // When
        assertThatCode(() -> userRoleService.assignRealmRole(userId, roleName, operatorKcId))
                .doesNotThrowAnyException();

        // Then
        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService).assignRealmRole(testUser.getKcUserId(), roleName);
    }

    @Test
    @DisplayName("测试2: 分配角色 - 失败 - 操作者不是管理员")
    void testAssignRealmRole_Fail_NotAdmin() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-user-123";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER"));

        // When & Then
        assertThatThrownBy(() -> userRoleService.assignRealmRole(userId, roleName, operatorKcId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有系统管理员可以分配Realm角色");

        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository, never()).findById(any());
        verify(keycloakAdminService, never()).assignRealmRole(any(), any());
    }

    @Test
    @DisplayName("测试3: 分配角色 - 失败 - 用户不存在")
    void testAssignRealmRole_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.assignRealmRole(userId, roleName, operatorKcId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService, never()).assignRealmRole(any(), any());
    }

    @Test
    @DisplayName("测试4: 分配角色 - 失败 - 无效的角色名称")
    void testAssignRealmRole_Fail_InvalidRole() {
        // Given
        Long userId = 1L;
        String roleName = "INVALID_ROLE";
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userRoleService.assignRealmRole(userId, roleName, operatorKcId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的角色名称");

        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService, never()).assignRealmRole(any(), any());
    }

    @Test
    @DisplayName("测试5: 批量分配角色 - 成功")
    void testAssignMultipleRealmRoles_Success() {
        // Given
        Long userId = 1L;
        List<String> roleNames = Arrays.asList("USER", "ADMIN");
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(keycloakAdminService).assignRealmRole(anyString(), anyString());

        // When
        assertThatCode(() -> userRoleService.assignMultipleRealmRoles(userId, roleNames, operatorKcId))
                .doesNotThrowAnyException();

        // Then
        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService, times(2)).assignRealmRole(eq(testUser.getKcUserId()), anyString());
    }

    // ========== 撤销角色测试 ==========

    @Test
    @DisplayName("测试10: 撤销角色 - 成功")
    void testWithdrawRealmRole_Success() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.getUserRealmRoles(testUser.getKcUserId()))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, adminUser));
        // 关键：模拟两个用户都有 ADMIN 角色，这样撤销一个不会触发"最后一个管理员"保护
        when(keycloakAdminService.hasRealmRole(testUser.getKcUserId(), "ADMIN"))
                .thenReturn(true);
        when(keycloakAdminService.hasRealmRole(adminUser.getKcUserId(), "ADMIN"))
                .thenReturn(true);
        doNothing().when(keycloakAdminService).removeRealmRole(testUser.getKcUserId(), roleName);

        // When
        assertThatCode(() -> userRoleService.withdrawRealmRole(userId, roleName, operatorKcId))
                .doesNotThrowAnyException();

        // Then
        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService).removeRealmRole(testUser.getKcUserId(), roleName);
    }

    @Test
    @DisplayName("测试11: 撤销角色 - 失败 - 操作者不是管理员")
    void testWithdrawRealmRole_Fail_NotAdmin() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-user-123";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER"));

        // When & Then
        assertThatThrownBy(() -> userRoleService.withdrawRealmRole(userId, roleName, operatorKcId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有系统管理员可以撤销Realm角色");

        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository, never()).findById(any());
        verify(keycloakAdminService, never()).removeRealmRole(any(), any());
    }

    @Test
    @DisplayName("测试12: 撤销角色 - 失败 - 不能撤销最后一个管理员")
    void testWithdrawRealmRole_Fail_LastAdmin() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.getUserRealmRoles(testUser.getKcUserId()))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(keycloakAdminService.hasRealmRole(testUser.getKcUserId(), "ADMIN"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userRoleService.withdrawRealmRole(userId, roleName, operatorKcId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("不能撤销最后一个系统管理员的ADMIN角色");

        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService, never()).removeRealmRole(any(), any());
    }

    @Test
    @DisplayName("测试13: 撤销角色 - 失败 - 用户不存在")
    void testWithdrawRealmRole_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        String roleName = "ADMIN";
        String operatorKcId = "kc-admin-456";

        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.withdrawRealmRole(userId, roleName, operatorKcId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(keycloakAdminService).getUserRealmRoles(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService, never()).removeRealmRole(any(), any());
    }

    // ========== 查询角色测试 ==========

    @Test
    @DisplayName("测试20: 查询用户角色 - 成功 - 查看自己")
    void testGetUserRealmRoles_Success_Self() {
        // Given
        Long userId = 1L;
        String operatorKcId = "kc-user-123";

        when(userRepository.findByKcUserId(operatorKcId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.getUserRealmRoles(testUser.getKcUserId()))
                .thenReturn(Arrays.asList("USER"));
        when(teamMemberRepository.findByUserAndStatus(testUser, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(Arrays.asList());

        // When
        UserRoleResponse response = userRoleService.getUserRealmRoles(userId, operatorKcId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRealmRoles()).contains("USER");

        verify(userRepository).findByKcUserId(operatorKcId);
        verify(userRepository).findById(userId);
        verify(keycloakAdminService).getUserRealmRoles(testUser.getKcUserId());
    }

    @Test
    @DisplayName("测试21: 查询用户角色 - 成功 - 管理员查看他人")
    void testGetUserRealmRoles_Success_AdminViewOthers() {
        // Given
        Long userId = 1L;
        String operatorKcId = "kc-admin-456";

        when(userRepository.findByKcUserId(operatorKcId)).thenReturn(Optional.of(adminUser));
        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.getUserRealmRoles(testUser.getKcUserId()))
                .thenReturn(Arrays.asList("USER"));
        when(teamMemberRepository.findByUserAndStatus(testUser, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(Arrays.asList());

        // When
        UserRoleResponse response = userRoleService.getUserRealmRoles(userId, operatorKcId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByKcUserId(operatorKcId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("测试22: 查询用户角色 - 失败 - 普通用户查看他人")
    void testGetUserRealmRoles_Fail_NotAuthorized() {
        // Given
        Long userId = 2L; // 查看其他用户
        String operatorKcId = "kc-user-123";

        when(userRepository.findByKcUserId(operatorKcId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER"));

        // When & Then
        assertThatThrownBy(() -> userRoleService.getUserRealmRoles(userId, operatorKcId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只能查看自己的角色信息");

        verify(userRepository).findByKcUserId(operatorKcId);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("测试23: 查询用户角色 - 失败 - 用户不存在")
    void testGetUserRealmRoles_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        String operatorKcId = "kc-admin-456";

        when(userRepository.findByKcUserId(operatorKcId)).thenReturn(Optional.of(adminUser));
        when(keycloakAdminService.getUserRealmRoles(operatorKcId))
                .thenReturn(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.getUserRealmRoles(userId, operatorKcId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository).findByKcUserId(operatorKcId);
        verify(userRepository).findById(userId);
    }

    // ========== 工具方法测试 ==========

    @Test
    @DisplayName("测试30: 检查用户是否拥有角色 - 有角色")
    void testHasRealmRole_True() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.hasRealmRole(testUser.getKcUserId(), roleName))
                .thenReturn(true);

        // When
        boolean result = userRoleService.hasRealmRole(userId, roleName);

        // Then
        assertThat(result).isTrue();

        verify(userRepository).findById(userId);
        verify(keycloakAdminService).hasRealmRole(testUser.getKcUserId(), roleName);
    }

    @Test
    @DisplayName("测试31: 检查用户是否拥有角色 - 没有角色")
    void testHasRealmRole_False() {
        // Given
        Long userId = 1L;
        String roleName = "ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(keycloakAdminService.hasRealmRole(testUser.getKcUserId(), roleName))
                .thenReturn(false);

        // When
        boolean result = userRoleService.hasRealmRole(userId, roleName);

        // Then
        assertThat(result).isFalse();

        verify(userRepository).findById(userId);
        verify(keycloakAdminService).hasRealmRole(testUser.getKcUserId(), roleName);
    }

    @Test
    @DisplayName("测试32: 检查用户是否拥有角色 - 用户不存在")
    void testHasRealmRole_UserNotFound() {
        // Given
        Long userId = 999L;
        String roleName = "ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.hasRealmRole(userId, roleName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository).findById(userId);
        verify(keycloakAdminService, never()).hasRealmRole(any(), any());
    }
}

