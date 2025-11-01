package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    public TeamMember addMember(Long teamId, String userKcId, String role, String operatorKcId) {
        log.info("Adding member to team: teamId={}, userKcId={}, role={}, operator={}",
                teamId, userKcId, role, operatorKcId);

        // 1. 验证操作者权限（必须是 OWNER 或 ADMIN）
        if (!hasManagePermission(teamId, operatorKcId)) {
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
        User user = userRepository.findByKcUserId(userKcId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userKcId));

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

        // 1. 验证操作者权限
        if (!hasManagePermission(teamId, operatorKcId)) {
            throw new SecurityException("只有团队拥有者或管理员可以移除成员");
        }

        // 2. 获取成员记录
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员记录不存在: " + memberId));

        // 3. 验证成员属于该团队
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("成员不属于该团队");
        }

        // 4. 不能移除团队拥有者
        if (member.getRole() == TeamMember.TeamRole.OWNER) {
            throw new IllegalArgumentException("不能移除团队拥有者");
        }

        // 5. 标记为已移除（软删除）
        member.setStatus(TeamMember.MemberStatus.REMOVED);
        teamMemberRepository.save(member);

        // 6. 更新团队成员数量
        Team team = member.getTeam();
        team.setMemberCount(Math.max(1, team.getMemberCount() - 1));
        teamRepository.save(team);

        log.info("Member removed successfully: teamId={}, memberId={}", teamId, memberId);
    }

    @Override
    public TeamMember updateMemberRole(Long teamId, Long memberId, String newRole, String operatorKcId) {
        log.info("Updating member role: teamId={}, memberId={}, newRole={}, operator={}",
                teamId, memberId, newRole, operatorKcId);

        // 1. 验证操作者是团队拥有者（只有拥有者可以修改角色）
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        User operator = userRepository.findByKcUserId(operatorKcId)
                .orElseThrow(() -> new RuntimeException("操作者不存在: " + operatorKcId));

        if (!team.getOwner().getId().equals(operator.getId())) {
            throw new SecurityException("只有团队拥有者可以修改成员角色");
        }

        // 2. 获取成员记录
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员记录不存在: " + memberId));

        // 3. 验证成员属于该团队
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("成员不属于该团队");
        }

        // 4. 不能修改拥有者的角色
        if (member.getRole() == TeamMember.TeamRole.OWNER) {
            throw new IllegalArgumentException("不能修改团队拥有者的角色");
        }

        // 5. 不能将成员设置为拥有者
        TeamMember.TeamRole newRoleEnum = TeamMember.TeamRole.valueOf(newRole.toUpperCase());
        if (newRoleEnum == TeamMember.TeamRole.OWNER) {
            throw new IllegalArgumentException("不能将成员设置为拥有者");
        }

        // 6. 更新角色
        member.setRole(newRoleEnum);
        member = teamMemberRepository.save(member);

        log.info("Member role updated successfully: teamId={}, memberId={}, newRole={}",
                teamId, memberId, newRole);

        return member;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMembers(Long teamId, String operatorKcId) {
        log.debug("Getting team members: teamId={}, operator={}", teamId, operatorKcId);

        // 验证操作者是团队成员
        if (!isMember(teamId, operatorKcId)) {
            throw new SecurityException("只有团队成员可以查看成员列表");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        return teamMemberRepository.findByTeamOrderByJoinedAtAsc(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMember> getActiveTeamMembers(Long teamId, String operatorKcId) {
        log.debug("Getting active team members: teamId={}, operator={}", teamId, operatorKcId);

        // 验证操作者是团队成员
        if (!isMember(teamId, operatorKcId)) {
            throw new SecurityException("只有团队成员可以查看成员列表");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

        return teamMemberRepository.findByTeamAndStatus(team, TeamMember.MemberStatus.ACTIVE);
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

        // 团队拥有者不能退出自己的团队
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
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return false;
        }

        User user = userRepository.findByKcUserId(userKcId).orElse(null);
        if (user == null) {
            return false;
        }

        return teamMemberRepository.existsByTeamAndUserAndStatus(team, user, TeamMember.MemberStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasManagePermission(Long teamId, String userKcId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return false;
        }

        User user = userRepository.findByKcUserId(userKcId).orElse(null);
        if (user == null) {
            return false;
        }

        Optional<TeamMember> member = teamMemberRepository.findByTeamAndUser(team, user);
        if (member.isEmpty() || member.get().getStatus() != TeamMember.MemberStatus.ACTIVE) {
            return false;
        }

        TeamMember.TeamRole role = member.get().getRole();
        return role == TeamMember.TeamRole.OWNER || role == TeamMember.TeamRole.ADMIN;
    }
}

