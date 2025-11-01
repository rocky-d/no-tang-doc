package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.team.TeamCreateRequest;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeamServiceImpl 核心单元测试
 * 测试最关键的业务逻辑
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamServiceImpl服务测试")
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
    @Order(1)
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
    @Order(2)
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
    @Order(3)
    @DisplayName("测试3: 创建团队失败 - 用户不存在")
    void testCreateTeam_Fail_UserNotFound(){
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("New Team")
                .description("test")
                .build();

        // user not found
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(request, kcUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository).findByKcUserId(kcUserId);
        verify(teamRepository, never()).save(any(Team.class));
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    @Order(10)
    @DisplayName("测试10: 根据ID获取团队成功")
    void testGetTeamById_Success() {
        // Given
        Team mockTeam = Team.builder()
                .id(10L)
                .name("AI团队")
                .description("测试团队")
                .build();

        when(teamRepository.findById(10L)).thenReturn(Optional.of(mockTeam));

        // When
        Team result = teamService.getTeamById(10L);

        // Then
        assertThat(result).isEqualTo(mockTeam);
        verify(teamRepository).findById(10L);
    }

    @Test
    @Order(11)
    @DisplayName("测试11: 根据ID获取团队失败 - 团队不存在")
    void testGetTeamById_Fail_NotFound() {
        // Given
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.getTeamById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("团队不存在");

        verify(teamRepository).findById(99L);
    }

    @Test
    @Order(20)
    @DisplayName("测试20: 获取用户拥有的团队成功")
    void testGetUserOwnedTeams_Success() {
        // Given
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));

        Team team1 = Team.builder().id(1L).name("团队A").owner(mockOwner).build();
        Team team2 = Team.builder().id(2L).name("团队B").owner(mockOwner).build();

        when(teamRepository.findByOwnerOrderByCreatedAtDesc(mockOwner))
                .thenReturn(List.of(team1, team2));

        // When
        List<Team> result = teamService.getUserOwnedTeams(kcUserId);

        // Then
        assertThat(result).hasSize(2)
                .extracting(Team::getName)
                .containsExactly("团队A", "团队B");

        verify(userRepository).findByKcUserId(kcUserId);
        verify(teamRepository).findByOwnerOrderByCreatedAtDesc(mockOwner);
    }

    @Test
    @Order(21)
    @DisplayName("测试21: 获取用户拥有的团队失败 - 用户不存在")
    void testGetUserOwnedTeams_Fail_UserNotFound() {
        // Given
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.getUserOwnedTeams(kcUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        // 验证调用关系
        verify(userRepository).findByKcUserId(kcUserId);
        verify(teamRepository, never()).findByOwnerOrderByCreatedAtDesc(any());
    }



    @Test
    @Order(30)
    @DisplayName("测试30: 更新团队成功 - 拥有者更新团队信息")
    void testUpdateTeam_Success() {
        // Given
        Long teamId = 1L;
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("新团队名称")
                .description("新描述")
                .build();

        Team existingTeam = Team.builder()
                .id(teamId)
                .name("旧团队名称")
                .description("旧描述")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));
        when(teamRepository.findByNameAndOwner(request.getName(), mockOwner)).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Team result = teamService.updateTeam(teamId, request, kcUserId);

        // Then
        assertThat(result.getName()).isEqualTo("新团队名称");
        assertThat(result.getDescription()).isEqualTo("新描述");
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @Order(31)
    @DisplayName("测试31: 更新团队失败 - 非拥有者无权限")
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

    @Test
    @Order(32)
    @DisplayName("测试32: 更新团队失败 - 团队名称重复")
    void testUpdateTeam_Fail_DuplicateName() {
        // Given
        Long teamId = 1L;
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("重复团队名称")
                .description("新描述")
                .build();

        // 模拟当前团队
        Team existingTeam = Team.builder()
                .id(teamId)
                .name("旧名称")
                .description("旧描述")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        // 模拟用户与重复团队
        Team duplicateTeam = Team.builder()
                .id(2L)
                .name("重复团队名称")
                .owner(mockOwner)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));
        when(teamRepository.findByNameAndOwner(request.getName(), mockOwner))
                .thenReturn(Optional.of(duplicateTeam));

        // When & Then
        assertThatThrownBy(() -> teamService.updateTeam(teamId, request, kcUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("团队名称已存在");

        verify(teamRepository, never()).save(any());
    }

    @Test
    @Order(33)
    @DisplayName("测试33: 更新团队失败 - 团队不存在")
    void testUpdateTeam_Fail_TeamNotFound() {
        // Given
        Long teamId = 1L;
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("新团队")
                .description("测试描述")
                .build();

        // Mock: 团队不存在
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.updateTeam(teamId, request, kcUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("团队不存在");

        verify(teamRepository).findById(teamId);
        verify(teamRepository, never()).save(any());
    }

    @Test
    @Order(34)
    @DisplayName("测试34: 更新团队 - 名称未改变时不触发重复检查")
    void testUpdateTeam_SameName_NoDuplicateCheck() {
        Long teamId = 1L;
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("同名团队")
                .description("新描述")
                .build();

        Team existingTeam = Team.builder()
                .id(teamId)
                .name("同名团队") // 与请求相同
                .description("旧描述")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // 不应该触发 findByNameAndOwner()
        Team result = teamService.updateTeam(teamId, request, kcUserId);

        assertThat(result.getDescription()).isEqualTo("新描述");
        verify(teamRepository, never()).findByNameAndOwner(anyString(), any());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @Order(35)
    @DisplayName("测试35: 更新团队 - 同名团队ID相同时不抛异常")
    void testUpdateTeam_SameNameWithSameId_NoException() {
        Long teamId = 1L;
        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("重复团队")
                .description("新描述")
                .build();

        Team existingTeam = Team.builder()
                .id(teamId)
                .name("旧名称")
                .description("旧描述")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        Team duplicateTeam = Team.builder()
                .id(teamId) // ID 相同！
                .name("重复团队")
                .owner(mockOwner)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));
        when(teamRepository.findByNameAndOwner(request.getName(), mockOwner))
                .thenReturn(Optional.of(duplicateTeam));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // 不应抛异常
        Team result = teamService.updateTeam(teamId, request, kcUserId);

        assertThat(result.getName()).isEqualTo("重复团队");
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @Order(40)
    @DisplayName("测试40: 删除团队成功 - 拥有者软删除团队")
    void testDeleteTeam_Success() {
        // Given
        Long teamId = 1L;
        Team existingTeam = Team.builder()
                .id(teamId)
                .name("AI团队")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        teamService.deleteTeam(teamId, kcUserId);

        // Then
        assertThat(existingTeam.getStatus()).isEqualTo(Team.TeamStatus.DELETED);
        verify(teamRepository).save(existingTeam);
    }

    @Test
    @Order(41)
    @DisplayName("测试41: 删除团队失败 - 非拥有者无权限")
    void testDeleteTeam_Fail_NotOwner() {
        // Given
        Long teamId = 1L;
        User realOwner = User.builder()
                .id(99L)
                .kcUserId("other-user-id")
                .username("owner")
                .build();

        Team existingTeam = Team.builder()
                .id(teamId)
                .name("测试团队")
                .owner(realOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));

        // When & Then
        assertThatThrownBy(() -> teamService.deleteTeam(teamId, kcUserId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只有团队拥有者可以删除团队");

        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @Order(42)
    @DisplayName("测试42: 删除团队失败 - 团队不存在")
    void testDeleteTeam_Fail_TeamNotFound() {
        // Given
        Long teamId = 1L;
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.deleteTeam(teamId, kcUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("团队不存在");

        verify(teamRepository).findById(teamId);
        verify(teamRepository, never()).save(any());
    }


    @Test
    @Order(50)
    @DisplayName("测试50: 获取用户活跃团队成功")
    void testGetUserActiveTeams_Success() {
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(mockOwner));

        Team activeTeam = Team.builder()
                .id(1L)
                .name("AI研发组")
                .owner(mockOwner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findByOwnerAndStatusOrderByCreatedAtDesc(mockOwner, Team.TeamStatus.ACTIVE))
                .thenReturn(List.of(activeTeam));

        List<Team> result = teamService.getUserActiveTeams(kcUserId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Team.TeamStatus.ACTIVE);

        verify(userRepository).findByKcUserId(kcUserId);
        verify(teamRepository).findByOwnerAndStatusOrderByCreatedAtDesc(mockOwner, Team.TeamStatus.ACTIVE);
    }

    @Test
    @Order(51)
    @DisplayName("测试51: 获取用户活跃团队失败 - 用户不存在")
    void testGetUserActiveTeams_Fail_UserNotFound() {
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getUserActiveTeams(kcUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository).findByKcUserId(kcUserId);
        verify(teamRepository, never()).findByOwnerAndStatusOrderByCreatedAtDesc(any(), any());
    }

}

