package com.ntdoc.notangdoccore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Team entity class
 * 团队实体类
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team", indexes = {
        @Index(name = "idx_team_owner_id", columnList = "owner_id"),
        @Index(name = "idx_team_status", columnList = "status"),
        @Index(name = "idx_team_created_at", columnList = "created_at")
})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    @Column(name = "member_count", nullable = false)
    @Builder.Default
    private Integer memberCount = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * 团队状态枚举
     */
    public enum TeamStatus {
        ACTIVE,    // 活跃
        ARCHIVED,  // 已归档
        DELETED    // 已删除
    }
}

