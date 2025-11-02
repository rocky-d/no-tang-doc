package com.ntdoc.notangdoccore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * TeamMember entity class
 * 团队成员关系实体类
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_team_member_team_user", columnNames = {"team_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_team_member_team_id", columnList = "team_id"),
                @Index(name = "idx_team_member_user_id", columnList = "user_id"),
                @Index(name = "idx_team_member_role", columnList = "role"),
                @Index(name = "idx_team_member_status", columnList = "status")
        })
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamRole role = TeamRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant joinedAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * 团队内角色枚举
     */
    public enum TeamRole {
        OWNER,   // 拥有者（创建者）
        ADMIN,   // 管理员
        MEMBER,  // 普通成员
        VIEWER   // 只读成员
    }

    /**
     * 成员状态枚举
     */
    public enum MemberStatus {
        ACTIVE,   // 活跃
        REMOVED,  // 已移除
        INVITED   // 已邀请（待接受）
    }
}

