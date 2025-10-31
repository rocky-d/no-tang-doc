package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 团队成员数据访问层
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * 根据团队查找所有成员
     */
    List<TeamMember> findByTeamOrderByJoinedAtAsc(Team team);

    /**
     * 根据团队和状态查找成员
     */
    List<TeamMember> findByTeamAndStatus(Team team, TeamMember.MemberStatus status);

    /**
     * 根据用户查找所有团队成员关系
     */
    List<TeamMember> findByUserOrderByJoinedAtDesc(User user);

    /**
     * 查找用户在某个团队中的成员记录
     */
    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    /**
     * 检查用户是否是团队成员
     */
    boolean existsByTeamAndUserAndStatus(Team team, User user, TeamMember.MemberStatus status);

    /**
     * 统计团队的活跃成员数量
     */
    long countByTeamAndStatus(Team team, TeamMember.MemberStatus status);

    /**
     * 根据团队和角色查找成员
     */
    List<TeamMember> findByTeamAndRole(Team team, TeamMember.TeamRole role);
}

