package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.team.TeamCreateRequest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeamServiceImpl 核心单元测试
 * 测试最关键的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("团队服务核心测试")
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User mockOwner;
    private String kcUserId;

    @BeforeEach
    void setUp() {
        kcUserId = "test-kc-user-id-123";
        mockOwner = User.builder()
                .id(1L)
                .kcUserId(kcUserId)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("测试1: 创建团队成功 - 自动添加创建者为OWNER")
    void testCreateTeam_Success_AutoAddOwner() {
        // Given: 准备测试数据
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("开发团队")
                .description("后端开发团队")
                .build();

        // Mock: 用户存在
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));

        // Mock: 团队名称不重复
        when(teamRepository.findByNameAndOwner(request.getName(), mockOwner))
                .thenReturn(Optional.empty());

        // Mock: 保存团队返回带ID的团队
        Team savedTeam = Team.builder()
                .id(1L)
                .name(request.getName())
                .description(request.getDescription())
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .memberCount(1)
                .build();
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        // Mock: 保存团队成员
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 调用创建团队方法
        Team result = teamService.createTeam(request, kcUserId);

        // Then: 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("开发团队");
        assertThat(result.getDescription()).isEqualTo("后端开发团队");
        assertThat(result.getOwner()).isEqualTo(mockOwner);
        assertThat(result.getStatus()).isEqualTo(Team.TeamStatus.ACTIVE);
        assertThat(result.getMemberCount()).isEqualTo(1);

        // 验证团队保存了一次
        verify(teamRepository, times(1)).save(any(Team.class));

        // 验证创建者自动被添加为团队成员，且角色为OWNER
        ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
        verify(teamMemberRepository, times(1)).save(memberCaptor.capture());

        TeamMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getTeam()).isEqualTo(savedTeam);
        assertThat(savedMember.getUser()).isEqualTo(mockOwner);
        assertThat(savedMember.getRole()).isEqualTo(TeamMember.TeamRole.OWNER);
        assertThat(savedMember.getStatus()).isEqualTo(TeamMember.MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("测试2: 创建团队失败 - 团队名称重复")
    void testCreateTeam_Fail_DuplicateName() {
        // Given: 准备测试数据
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("开发团队")
                .description("后端开发团队")
                .build();

        // Mock: 用户存在
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));

        // Mock: 团队名称已存在
        Team existingTeam = Team.builder()
                .id(99L)
                .name("开发团队")
                .owner(mockOwner)
                .build();
        when(teamRepository.findByNameAndOwner(request.getName(), mockOwner))
                .thenReturn(Optional.of(existingTeam));

        // When & Then: 调用方法应该抛出异常
        assertThatThrownBy(() -> teamService.createTeam(request, kcUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("团队名称已存在");

        // 验证没有保存任何团队
        verify(teamRepository, never()).save(any(Team.class));
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("测试3: 更新团队失败 - 非拥有者无权限")
    void testUpdateTeam_Fail_NotOwner() {
        // Given: 准备测试数据
        Long teamId = 1L;
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("新团队名称")
                .description("新描述")
                .build();

        // 团队的真实拥有者（不是当前用户）
        User realOwner = User.builder()
                .id(999L)
                .kcUserId("other-user-id")
                .username("otheruser")
                .build();

        Team existingTeam = Team.builder()
                .id(teamId)
                .name("旧团队名称")
                .description("旧描述")
                .owner(realOwner) // 注意：拥有者不是当前用户
                .status(Team.TeamStatus.ACTIVE)
                .build();

        // Mock: 团队存在
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));

        // Mock: 当前用户存在（但不是团队拥有者）
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));

        // When & Then: 调用更新方法应该抛出权限异常
        assertThatThrownBy(() -> teamService.updateTeam(teamId, request, kcUserId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有团队拥有者可以更新团队信息");

        // 验证没有保存任何更新
        verify(teamRepository, never()).save(any(Team.class));
    }
}

