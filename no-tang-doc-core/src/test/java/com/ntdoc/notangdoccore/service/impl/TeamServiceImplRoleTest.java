package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 团队服务 - 用户角色查询功能测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService - 用户角色查询测试")
class TeamServiceImplRoleTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    @DisplayName("测试获取用户角色 - 用户是OWNER")
    void testGetUserRoleInTeam_UserIsOwner() {
        // Arrange
        Long teamId = 1L;
        String kcUserId = "kc-user-123";

        User user = User.builder()
                .id(1L)
                .kcUserId(kcUserId)
                .username("testuser")
                .build();

        Team team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .owner(user)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        TeamMember teamMember = TeamMember.builder()
                .id(1L)
                .team(team)
                .user(user)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByTeamAndUserAndStatus(any(), any(), any()))
                .thenReturn(Optional.of(teamMember));

        // Act
        String role = teamService.getUserRoleInTeam(teamId, kcUserId);

        // Assert
        assertThat(role).isEqualTo("OWNER");
    }

    @Test
    @DisplayName("测试获取用户角色 - 用户是ADMIN")
    void testGetUserRoleInTeam_UserIsAdmin() {
        // Arrange
        Long teamId = 1L;
        String kcUserId = "kc-user-456";

        User owner = User.builder()
                .id(1L)
                .kcUserId("kc-owner-123")
                .username("owner")
                .build();

        User admin = User.builder()
                .id(2L)
                .kcUserId(kcUserId)
                .username("admin")
                .build();

        Team team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .owner(owner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        TeamMember teamMember = TeamMember.builder()
                .id(2L)
                .team(team)
                .user(admin)
                .role(TeamMember.TeamRole.ADMIN)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(admin));
        when(teamMemberRepository.findByTeamAndUserAndStatus(any(), any(), any()))
                .thenReturn(Optional.of(teamMember));

        // Act
        String role = teamService.getUserRoleInTeam(teamId, kcUserId);

        // Assert
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("测试获取用户角色 - 用户不是团队成员")
    void testGetUserRoleInTeam_UserNotMember() {
        // Arrange
        Long teamId = 1L;
        String kcUserId = "kc-user-999";

        User owner = User.builder()
                .id(1L)
                .kcUserId("kc-owner-123")
                .username("owner")
                .build();

        User nonMember = User.builder()
                .id(3L)
                .kcUserId(kcUserId)
                .username("nonmember")
                .build();

        Team team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .owner(owner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(nonMember));
        when(teamMemberRepository.findByTeamAndUserAndStatus(any(), any(), any()))
                .thenReturn(Optional.empty());

        // Act
        String role = teamService.getUserRoleInTeam(teamId, kcUserId);

        // Assert
        assertThat(role).isNull();
    }

    @Test
    @DisplayName("测试检查是否是团队成员 - 是成员")
    void testIsTeamMember_True() {
        // Arrange
        Long teamId = 1L;
        String kcUserId = "kc-user-123";

        User user = User.builder()
                .id(1L)
                .kcUserId(kcUserId)
                .username("testuser")
                .build();

        Team team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .owner(user)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        TeamMember teamMember = TeamMember.builder()
                .id(1L)
                .team(team)
                .user(user)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByTeamAndUserAndStatus(any(), any(), any()))
                .thenReturn(Optional.of(teamMember));

        // Act
        boolean isMember = teamService.isTeamMember(teamId, kcUserId);

        // Assert
        assertThat(isMember).isTrue();
    }

    @Test
    @DisplayName("测试检查是否是团队成员 - 不是成员")
    void testIsTeamMember_False() {
        // Arrange
        Long teamId = 1L;
        String kcUserId = "kc-user-999";

        User owner = User.builder()
                .id(1L)
                .kcUserId("kc-owner-123")
                .username("owner")
                .build();

        User nonMember = User.builder()
                .id(3L)
                .kcUserId(kcUserId)
                .username("nonmember")
                .build();

        Team team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .owner(owner)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findByKcUserId(kcUserId)).thenReturn(Optional.of(nonMember));
        when(teamMemberRepository.findByTeamAndUserAndStatus(any(), any(), any()))
                .thenReturn(Optional.empty());

        // Act
        boolean isMember = teamService.isTeamMember(teamId, kcUserId);

        // Assert
        assertThat(isMember).isFalse();
    }
}

