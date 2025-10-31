package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.DocumentComment;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档评论数据访问层
 */
@Repository
public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

    /**
     * 查询指定文档的所有活跃评论
     */
    List<DocumentComment> findByDocumentAndStatusOrderByCreatedAtDesc(
            Document document,
            DocumentComment.CommentStatus status
    );

    /**
     * 查询指定文档和团队的所有活跃评论
     */
    List<DocumentComment> findByDocumentAndTeamAndStatusOrderByCreatedAtDesc(
            Document document,
            Team team,
            DocumentComment.CommentStatus status
    );

    /**
     * 查询用户在指定文档下的所有评论
     */
    List<DocumentComment> findByDocumentAndUserOrderByCreatedAtDesc(
            Document document,
            User user
    );

    /**
     * 统计指定文档的评论数量
     */
    @Query("SELECT COUNT(c) FROM DocumentComment c WHERE c.document = :document AND c.status = :status")
    long countByDocumentAndStatus(
            @Param("document") Document document,
            @Param("status") DocumentComment.CommentStatus status
    );

    /**
     * 查询指定父评论的所有回复
     */
    List<DocumentComment> findByParentCommentAndStatusOrderByCreatedAtAsc(
            DocumentComment parentComment,
            DocumentComment.CommentStatus status
    );

    /**
     * 检查用户是否对文档有评论权限（是否是团队成员）
     */
    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END " +
           "FROM TeamMember tm " +
           "WHERE tm.team = :team AND tm.user = :user AND tm.status = 'ACTIVE'")
    boolean isUserTeamMember(@Param("team") Team team, @Param("user") User user);
}

