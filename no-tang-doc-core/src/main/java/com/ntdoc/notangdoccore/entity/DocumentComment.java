package com.ntdoc.notangdoccore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 文档评论实体类
 * 用于团队成员对文档进行评论和讨论
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_comment", indexes = {
        @Index(name = "idx_document_comment_document", columnList = "document_id"),
        @Index(name = "idx_document_comment_user", columnList = "user_id"),
        @Index(name = "idx_document_comment_team", columnList = "team_id"),
        @Index(name = "idx_document_comment_created_at", columnList = "created_at")
})
public class DocumentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private DocumentComment parentComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 评论状态枚举
     */
    public enum CommentStatus {
        /**
         * 活跃状态 - 评论正常显示
         */
        ACTIVE,

        /**
         * 已删除 - 评论被软删除
         */
        DELETED,

        /**
         * 已隐藏 - 评论被管理员隐藏
         */
        HIDDEN
    }
}

