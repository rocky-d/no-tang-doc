package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 团队数据访问层
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 根据拥有者查找所有团队
     */
    List<Team> findByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * 根据拥有者和状态查找团队
     */
    List<Team> findByOwnerAndStatusOrderByCreatedAtDesc(User owner, Team.TeamStatus status);

    /**
     * 根据团队名称和拥有者查找（用于检查重名）
     */
    Optional<Team> findByNameAndOwner(String name, User owner);

    /**
     * 统计用户拥有的团队数量
     */
    long countByOwnerAndStatus(User owner, Team.TeamStatus status);

    /**
     * 查找活跃团队
     */
    @Query("SELECT t FROM Team t WHERE t.status = 'ACTIVE' ORDER BY t.createdAt DESC")
    List<Team> findAllActiveTeams();
}

