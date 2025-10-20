package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.team.TeamCreateRequest;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.TeamMemberRepository;
import com.ntdoc.notangdoccore.repository.TeamRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.TeamService;
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
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    public Team createTeam(TeamCreateRequest request, String kcUserId) {
        log.info("Creating team: name={}, kcUserId={}", request.getName(), kcUserId);

        // 1. 获取用户
        User owner = getUserByKcUserId(kcUserId);

        // 2. 检查团队名称是否重复（同一用户下）
        Optional<Team> existingTeam = teamRepository.findByNameAndOwner(request.getName(), owner);
        if (existingTeam.isPresent()) {
            log.warn("Team name already exists for user: name={}, userId={}", request.getName(), owner.getId());
            throw new IllegalArgumentException("团队名称已存在");
        }

        // 3. 创建团队
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .status(Team.TeamStatus.ACTIVE)
                .memberCount(1) // 初始成员数为1（创建者）
                .build();

        team = teamRepository.save(team);
        log.info("Team created successfully: teamId={}, name={}", team.getId(), team.getName());

        // 4. 自动添加创建者为团队成员（角色为OWNER）
        TeamMember ownerMember = TeamMember.builder()
                .team(team)
                .user(owner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .build();

        teamMemberRepository.save(ownerMember);
        log.info("Team owner added as member: teamId={}, userId={}", team.getId(), owner.getId());

        return team;
    }

    @Override
    @Transactional(readOnly = true)
    public Team getTeamById(Long teamId) {
        log.debug("Getting team by id: {}", teamId);
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getUserOwnedTeams(String kcUserId) {
        log.debug("Getting teams owned by user: {}", kcUserId);
        User owner = getUserByKcUserId(kcUserId);
        return teamRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getUserActiveTeams(String kcUserId) {
        log.debug("Getting active teams owned by user: {}", kcUserId);
        User owner = getUserByKcUserId(kcUserId);
        return teamRepository.findByOwnerAndStatusOrderByCreatedAtDesc(owner, Team.TeamStatus.ACTIVE);
    }

    @Override
    public Team updateTeam(Long teamId, TeamCreateRequest request, String kcUserId) {
        log.info("Updating team: teamId={}, kcUserId={}", teamId, kcUserId);

        // 1. 获取团队
        Team team = getTeamById(teamId);

        // 2. 验证权限（只有拥有者可以更新）
        if (!isTeamOwner(teamId, kcUserId)) {
            log.warn("User is not team owner: teamId={}, kcUserId={}", teamId, kcUserId);
            throw new SecurityException("只有团队拥有者可以更新团队信息");
        }

        // 3. 检查新名称是否重复（如果名称有变化）
        if (!team.getName().equals(request.getName())) {
            User owner = getUserByKcUserId(kcUserId);
            Optional<Team> existingTeam = teamRepository.findByNameAndOwner(request.getName(), owner);
            if (existingTeam.isPresent() && !existingTeam.get().getId().equals(teamId)) {
                throw new IllegalArgumentException("团队名称已存在");
            }
        }

        // 4. 更新团队信息
        team.setName(request.getName());
        team.setDescription(request.getDescription());

        team = teamRepository.save(team);
        log.info("Team updated successfully: teamId={}", teamId);

        return team;
    }

    @Override
    public void deleteTeam(Long teamId, String kcUserId) {
        log.info("Deleting team: teamId={}, kcUserId={}", teamId, kcUserId);

        // 1. 获取团队
        Team team = getTeamById(teamId);

        // 2. 验证权限（只有拥有者可以删除）
        if (!isTeamOwner(teamId, kcUserId)) {
            log.warn("User is not team owner: teamId={}, kcUserId={}", teamId, kcUserId);
            throw new SecurityException("只有团队拥有者可以删除团队");
        }

        // 3. 软删除（标记为DELETED）
        team.setStatus(Team.TeamStatus.DELETED);
        teamRepository.save(team);

        log.info("Team deleted successfully: teamId={}", teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeamOwner(Long teamId, String kcUserId) {
        Team team = getTeamById(teamId);
        User user = getUserByKcUserId(kcUserId);
        return team.getOwner().getId().equals(user.getId());
    }

    /**
     * 根据 Keycloak 用户ID 获取用户
     */
    private User getUserByKcUserId(String kcUserId) {
        return userRepository.findByKcUserId(kcUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + kcUserId));
    }
}

