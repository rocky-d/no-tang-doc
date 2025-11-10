package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.team.TeamMemberResponse;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.TeamMemberService;
import com.ntdoc.notangdoccore.service.TeamPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamPermissionService permissionService;  // ✅ 新增权限服务

    @Override
    public TeamMember addMember(Long teamId, String userEmail, String role, String operatorKcId) {
        log.info("Adding member to team: teamId={}, userKcId={}, role={}, operator={}",
                teamId, userEmail, role, operatorKcId);

        // 1. 使用权限服务验证操作者权限
        if (!permissionService.hasInviteMemberPermission(teamId, operatorKcId)) {
            throw new SecurityException("只有团队拥有者或管理员可以添加成员");
        }

        // 2. 获取团队
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        // 3. 检查团队状态
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new IllegalStateException("只能向活跃的团队添加成员");
        }

        // 4. 获取要添加的用户
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userEmail));

        // 5. 检查用户是否已经是团队成员
        Optional<TeamMember> existingMember = teamMemberRepository.findByTeamAndUser(team, user);
        if (existingMember.isPresent()) {
            TeamMember member = existingMember.get();
            if (member.getStatus() == TeamMember.MemberStatus.ACTIVE) {
                throw new IllegalArgumentException("用户已经是团队成员");
            }
            // 如果之前被移除，重新激活
            member.setStatus(TeamMember.MemberStatus.ACTIVE);
            member.setRole(TeamMember.TeamRole.valueOf(role.toUpperCase()));
            log.info("Reactivated member: teamId={}, userId={}", teamId, user.getId());
            return teamMemberRepository.save(member);
        }

        // 6. 创建新成员记录
        TeamMember newMember = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamMember.TeamRole.valueOf(role.toUpperCase()))
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        newMember = teamMemberRepository.save(newMember);

        // 7. 更新团队成员数量
        team.setMemberCount(team.getMemberCount() + 1);
        teamRepository.save(team);

        log.info("Member added successfully: teamId={}, userId={}, memberId={}",
                teamId, user.getId(), newMember.getId());

        return newMember;
    }

    @Override
    public void removeMember(Long teamId, Long memberId, String operatorKcId) {
        log.info("Removing member from team: teamId={}, memberId={}, operator={}",
                teamId, memberId, operatorKcId);

        // 1. 使用权限服务验证是否可以移除
        if (!permissionService.canRemoveMember(teamId, memberId, operatorKcId)) {
            throw new SecurityException("您没有权限移除该成员");
        }

        // 2. 获取成员记录
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员记录不存在: " + memberId));

        // 3. 验证成员属于该团队
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("成员不属于该团队");
        }

        // 4. 标记为已移除（软删除）
        member.setStatus(TeamMember.MemberStatus.REMOVED);
        teamMemberRepository.save(member);

        // 5. 更新团队成员数量
        Team team = member.getTeam();
        team.setMemberCount(Math.max(1, team.getMemberCount() - 1));
        teamRepository.save(team);

        log.info("Member removed successfully: teamId={}, memberId={}", teamId, memberId);
    }

    @Override
    public TeamMemberResponse updateMemberRole(Long teamId, Long memberId, String newRole, String operatorKcId) {
        log.info("Updating member role: teamId={}, memberId={}, newRole={}, operator={}",
                teamId, memberId, newRole, operatorKcId);

        // 1. 使用权限服务验证操作者权限
        if (!permissionService.hasModifyRolePermission(teamId, operatorKcId)) {
            throw new SecurityException("只有团队拥有者可以修改成员角色");
        }

        // 2. 获取成员记录
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员记录不存在: " + memberId));

        // 3. 验证成员属于该团队
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("成员不属于该团队");
        }

        // 4. 验证角色变更的合法性
        TeamMember.TeamRole operatorRole = permissionService.getUserRole(teamId, operatorKcId);
        TeamMember.TeamRole newRoleEnum = TeamMember.TeamRole.valueOf(newRole.toUpperCase());

        if (!permissionService.isRoleChangeAllowed(operatorRole, member.getRole(), newRoleEnum)) {
            throw new IllegalArgumentException("不允许该角色变更");
        }

        // 5. 更新角色
        member.setRole(newRoleEnum);
        member = teamMemberRepository.save(member);

        TeamMemberResponse response = TeamMemberResponse.fromEntity(member);

        log.info("Member role updated successfully: teamId={}, memberId={}, newRole={}",
                teamId, memberId, newRole);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId, String operatorKcId) {
        log.debug("Getting team members: teamId={}, operator={}", teamId, operatorKcId);

        // 使用权限服务验证操作者是团队成员
        TeamMember.TeamRole role = permissionService.getUserRole(teamId, operatorKcId);
        if (role == null) {
            throw new SecurityException("只有团队成员可以查看成员列表");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        List<TeamMember> teamMembers = teamMemberRepository.findByTeamOrderByJoinedAtAsc(team);

        return teamMembers.stream()
                .map(TeamMemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getActiveTeamMembers(Long teamId, String operatorKcId) {
        log.debug("Getting active team members: teamId={}, operator={}", teamId, operatorKcId);

        // 使用权限服务验证操作者是团队成员
        TeamMember.TeamRole role = permissionService.getUserRole(teamId, operatorKcId);
        if (role == null) {
            throw new SecurityException("只有团队成员可以查看成员列表");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        List<TeamMember> teamMembers = teamMemberRepository.findByTeamAndStatus(team, TeamMember.MemberStatus.ACTIVE);

        return teamMembers.stream()
                .map(TeamMemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void leaveTeam(Long teamId, String userKcId) {
        log.info("User leaving team: teamId={}, userKcId={}", teamId, userKcId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        User user = userRepository.findByKcUserId(userKcId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userKcId));

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new RuntimeException("您不是该团队成员"));

        // 使用权限服务检查角色
        if (member.getRole() == TeamMember.TeamRole.OWNER) {
            throw new IllegalArgumentException("团队拥有者不能退出团队，请先转让团队或删除团队");
        }

        // 标记为已移除
        member.setStatus(TeamMember.MemberStatus.REMOVED);
        teamMemberRepository.save(member);

        // 更新团队成员数量
        team.setMemberCount(Math.max(1, team.getMemberCount() - 1));
        teamRepository.save(team);

        log.info("User left team successfully: teamId={}, userId={}", teamId, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(Long teamId, String userKcId) {
        return permissionService.getUserRole(teamId, userKcId) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasManagePermission(Long teamId, String userKcId) {
        return permissionService.hasManageMemberPermission(teamId, userKcId);
    }
}
