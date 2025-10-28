package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeamMemberServiceImpl 核心单元测试
 * 测试团队成员管理的关键业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("团队成员服务核心测试")
class TeamMemberServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamMemberServiceImpl teamMemberService;

    private User mockOwner;
    private User mockAdmin;
    private User mockNewUser;
    private Team mockTeam;
    private String ownerKcId;
    private String adminKcId;
    private String newUserKcId;

    @BeforeEach
    void setUp() {
        // 创建团队拥有者
        ownerKcId = "owner-kc-id-123";
        mockOwner = User.builder()
                .id(1L)
                .kcUserId(ownerKcId)
                .username("owner")
                .email("owner@example.com")
                .build();

        // 创建管理员
        adminKcId = "admin-kc-id-456";
        mockAdmin = User.builder()
                .id(2L)
                .kcUserId(adminKcId)
                .username("admin")
                .email("admin@example.com")
                .build();

        // 创建新用户
        newUserKcId = "new-user-kc-id-789";
        mockNewUser = User.builder()
                .id(3L)
                .kcUserId(newUserKcId)
                .username("newuser")
                .email("newuser@example.com")
                .build();

        // 创建测试团队
        mockTeam = Team.builder()
                .id(1L)
                .name("测试团队")
                .description("测试描述")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .memberCount(2)
                .build();
    }

    @Test
    @DisplayName("测试1: 添加成员成功 - OWNER添加新成员")
    void testAddMember_Success_OwnerAddsNewMember() {
        // Given: OWNER 添加新成员
        String newRole = "MEMBER";

        // Mock: 团队存在
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));

        // Mock: 新用户存在
        when(userRepository.findByKcUserId(newUserKcId)).thenReturn(Optional.of(mockNewUser));

        // Mock: 用户不是成员
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockNewUser))
                .thenReturn(Optional.empty());

        // Mock: OWNER 有管理权限
        TeamMember ownerMember = TeamMember.builder()
                .team(mockTeam)
                .user(mockOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockOwner))
                .thenReturn(Optional.of(ownerMember));
        lenient().when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockOwner, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(true);

        // Mock: 保存新成员
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> {
            TeamMember member = invocation.getArgument(0);
            member.setId(10L);
            return member;
        });

        // Mock: 保存团队
        when(teamRepository.save(any(Team.class))).thenReturn(mockTeam);

        // When: 添加成员
        TeamMember result = teamMemberService.addMember(1L, newUserKcId, newRole, ownerKcId);

        // Then: 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(mockNewUser);
        assertThat(result.getRole()).isEqualTo(TeamMember.TeamRole.MEMBER);
        assertThat(result.getStatus()).isEqualTo(TeamMember.MemberStatus.ACTIVE);

        // 验证保存操作
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
        verify(teamRepository, times(1)).save(mockTeam);
    }

    @Test
    @DisplayName("测试2: 添加成员失败 - 无权限用户尝试添加")
    void testAddMember_Fail_NoPermission() {
        // Given: 普通用户尝试添加成员
        String unauthorizedKcId = "unauthorized-user";
        User unauthorizedUser = User.builder()
                .id(99L)
                .kcUserId(unauthorizedKcId)
                .build();

        // Mock: 操作者不是成员或没有权限
        lenient().when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        lenient().when(userRepository.findByKcUserId(unauthorizedKcId)).thenReturn(Optional.of(unauthorizedUser));
        lenient().when(teamMemberRepository.existsByTeamAndUserAndStatus(any(), any(), any())).thenReturn(false);

        // When & Then: 应该抛出权限异常
        assertThatThrownBy(() -> teamMemberService.addMember(1L, newUserKcId, "MEMBER", unauthorizedKcId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有团队拥有者或管理员可以添加成员");

        // 验证没有保存任何数据
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("测试3: 添加成员失败 - 用户已存在")
    void testAddMember_Fail_UserAlreadyExists() {
        // Given: 用户已经是活跃成员
        TeamMember existingMember = TeamMember.builder()
                .id(5L)
                .team(mockTeam)
                .user(mockNewUser)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock: 权限验证通过
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));
        lenient().when(userRepository.findByKcUserId(newUserKcId)).thenReturn(Optional.of(mockNewUser));

        TeamMember ownerMember = TeamMember.builder()
                .team(mockTeam)
                .user(mockOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockOwner))
                .thenReturn(Optional.of(ownerMember));
        lenient().when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockOwner, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(true);

        // Mock: 用户已存在
        lenient().when(teamMemberRepository.findByTeamAndUser(mockTeam, mockNewUser))
                .thenReturn(Optional.of(existingMember));

        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> teamMemberService.addMember(1L, newUserKcId, "MEMBER", ownerKcId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户已经是团队成员");
    }

    @Test
    @DisplayName("测试4: 移除成员成功")
    void testRemoveMember_Success() {
        // Given: OWNER 移除普通成员
        Long memberId = 5L;
        TeamMember memberToRemove = TeamMember.builder()
                .id(memberId)
                .team(mockTeam)
                .user(mockNewUser)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock: 权限验证
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));

        TeamMember ownerMember = TeamMember.builder()
                .team(mockTeam)
                .user(mockOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockOwner))
                .thenReturn(Optional.of(ownerMember));
        lenient().when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockOwner, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(true);

        // Mock: 成员存在
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(memberToRemove));
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(memberToRemove);
        when(teamRepository.save(any(Team.class))).thenReturn(mockTeam);

        // When: 移除成员
        teamMemberService.removeMember(1L, memberId, ownerKcId);

        // Then: 验证成员状态被设置为 REMOVED
        ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
        verify(teamMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getStatus()).isEqualTo(TeamMember.MemberStatus.REMOVED);

        // 验证团队成员数量减少
        verify(teamRepository).save(mockTeam);
    }

    @Test
    @DisplayName("测试5: 移除成员失败 - 不能移除OWNER")
    void testRemoveMember_Fail_CannotRemoveOwner() {
        // Given: 尝试移除 OWNER
        Long ownerMemberId = 1L;
        TeamMember ownerMember = TeamMember.builder()
                .id(ownerMemberId)
                .team(mockTeam)
                .user(mockOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock: 权限验证通过
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockOwner))
                .thenReturn(Optional.of(ownerMember));
        lenient().when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockOwner, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(true);

        // Mock: 成员存在
        when(teamMemberRepository.findById(ownerMemberId)).thenReturn(Optional.of(ownerMember));

        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> teamMemberService.removeMember(1L, ownerMemberId, ownerKcId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能移除团队拥有者");

        // 验证没有保存操作
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("测试6: 更新成员角色成功")
    void testUpdateMemberRole_Success() {
        // Given: OWNER 将 MEMBER 升级为 ADMIN
        Long memberId = 5L;
        TeamMember memberToUpdate = TeamMember.builder()
                .id(memberId)
                .team(mockTeam)
                .user(mockNewUser)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock: 团队和操作者
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));

        // Mock: 成员存在
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(memberToUpdate));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 更新角色
        TeamMember result = teamMemberService.updateMemberRole(1L, memberId, "ADMIN", ownerKcId);

        // Then: 验证角色已更新
        assertThat(result.getRole()).isEqualTo(TeamMember.TeamRole.ADMIN);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("测试7: 更新成员角色失败 - 非OWNER无权限")
    void testUpdateMemberRole_Fail_NotOwner() {
        // Given: ADMIN 尝试修改角色（只有 OWNER 可以）
        Long memberId = 5L;
        TeamMember memberToUpdate = TeamMember.builder()
                .id(memberId)
                .team(mockTeam)
                .user(mockNewUser)
                .role(TeamMember.TeamRole.MEMBER)
                .build();

        // Mock: 团队存在，但操作者不是 OWNER
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(adminKcId)).thenReturn(Optional.of(mockAdmin));

        // When & Then: 应该抛出权限异常
        assertThatThrownBy(() -> teamMemberService.updateMemberRole(1L, memberId, "ADMIN", adminKcId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有团队拥有者可以修改成员角色");

        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("测试8: 更新成员角色失败 - 不能设置为OWNER")
    void testUpdateMemberRole_Fail_CannotSetOwner() {
        // Given: 尝试将成员设置为 OWNER
        Long memberId = 5L;
        TeamMember memberToUpdate = TeamMember.builder()
                .id(memberId)
                .team(mockTeam)
                .user(mockNewUser)
                .role(TeamMember.TeamRole.MEMBER)
                .build();

        // Mock: 团队和操作者
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));
        when(teamMemberRepository.findById(memberId)).thenReturn(Optional.of(memberToUpdate));

        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> teamMemberService.updateMemberRole(1L, memberId, "OWNER", ownerKcId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能将成员设置为拥有者");
    }

    @Test
    @DisplayName("测试9: 获取团队成员列表成功")
    void testGetActiveTeamMembers_Success() {
        // Given: 团队有多个活跃成员
        TeamMember member1 = TeamMember.builder()
                .id(1L)
                .team(mockTeam)
                .user(mockOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        TeamMember member2 = TeamMember.builder()
                .id(2L)
                .team(mockTeam)
                .user(mockAdmin)
                .role(TeamMember.TeamRole.ADMIN)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock: 权限验证
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));
        when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockOwner, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(true);

        // Mock: 返回成员列表
        when(teamMemberRepository.findByTeamAndStatus(mockTeam, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(Arrays.asList(member1, member2));

        // When: 获取成员列表
        List<TeamMember> result = teamMemberService.getActiveTeamMembers(1L, ownerKcId);

        // Then: 验证结果
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRole()).isEqualTo(TeamMember.TeamRole.OWNER);
        assertThat(result.get(1).getRole()).isEqualTo(TeamMember.TeamRole.ADMIN);
    }

    @Test
    @DisplayName("测试10: 获取团队成员列表失败 - 非成员无权限")
    void testGetActiveTeamMembers_Fail_NotMember() {
        // Given: 非成员尝试查看
        String outsiderKcId = "outsider-kc-id";
        User outsider = User.builder()
                .id(99L)
                .kcUserId(outsiderKcId)
                .build();

        // Mock: 用户不是成员
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(outsiderKcId)).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, outsider, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(false);

        // When & Then: 应该抛出权限异常
        assertThatThrownBy(() -> teamMemberService.getActiveTeamMembers(1L, outsiderKcId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有团队成员可以查看成员列表");
    }

    @Test
    @DisplayName("测试11: 成员主动退出团队成功")
    void testLeaveTeam_Success() {
        // Given: 普通成员退出团队
        TeamMember memberToLeave = TeamMember.builder()
                .id(5L)
                .team(mockTeam)
                .user(mockNewUser)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(newUserKcId)).thenReturn(Optional.of(mockNewUser));
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockNewUser))
                .thenReturn(Optional.of(memberToLeave));
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(memberToLeave);
        when(teamRepository.save(any(Team.class))).thenReturn(mockTeam);

        // When: 成员退出
        teamMemberService.leaveTeam(1L, newUserKcId);

        // Then: 验证状态更新为 REMOVED
        ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
        verify(teamMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getStatus()).isEqualTo(TeamMember.MemberStatus.REMOVED);
    }

    @Test
    @DisplayName("测试12: OWNER不能退出自己的团队")
    void testLeaveTeam_Fail_OwnerCannotLeave() {
        // Given: OWNER 尝试退出
        TeamMember ownerMember = TeamMember.builder()
                .id(1L)
                .team(mockTeam)
                .user(mockOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        // Mock
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        when(userRepository.findByKcUserId(ownerKcId)).thenReturn(Optional.of(mockOwner));
        when(teamMemberRepository.findByTeamAndUser(mockTeam, mockOwner))
                .thenReturn(Optional.of(ownerMember));

        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> teamMemberService.leaveTeam(1L, ownerKcId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("团队拥有者不能退出团队");

        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
}
