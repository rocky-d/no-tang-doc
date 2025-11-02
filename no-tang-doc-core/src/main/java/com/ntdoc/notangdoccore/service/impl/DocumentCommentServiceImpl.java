package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.*;
import com.ntdoc.notangdoccore.repository.*;
import com.ntdoc.notangdoccore.service.DocumentCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentCommentServiceImpl implements DocumentCommentService {

    private final DocumentCommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public DocumentComment createComment(Long documentId, Long teamId, String content,
                                        Long parentCommentId, String userKcId) {
        log.info("Creating comment for document: documentId={}, teamId={}, userKcId={}",
                documentId, teamId, userKcId);

        // 1. 获取文档
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + documentId));

        // 2. 获取用户
        User user = userRepository.findByKcUserId(userKcId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userKcId));

        // 3. 如果指定了团队，验证用户是否是团队成员
        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

            boolean isMember = teamMemberRepository.existsByTeamAndUserAndStatus(
                    team, user, TeamMember.MemberStatus.ACTIVE);

            if (!isMember) {
                throw new SecurityException("您不是该团队成员，无法评论");
            }
        }

        // 4. 如果是回复评论，验证父评论是否存在
        DocumentComment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("父评论不存在: " + parentCommentId));

            if (!parentComment.getDocument().getId().equals(documentId)) {
                throw new IllegalArgumentException("父评论不属于该文档");
            }
        }

        // 5. 创建评论
        DocumentComment comment = DocumentComment.builder()
                .document(document)
                .user(user)
                .team(team)
                .content(content)
                .parentComment(parentComment)
                .status(DocumentComment.CommentStatus.ACTIVE)
                .build();

        comment = commentRepository.save(comment);

        log.info("Comment created successfully: commentId={}, documentId={}",
                comment.getId(), documentId);

        return comment;
    }

    @Override
    public DocumentComment updateComment(Long commentId, String content, String userKcId) {
        log.info("Updating comment: commentId={}, userKcId={}", commentId, userKcId);

        // 1. 获取评论
        DocumentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        // 2. 获取用户
        User user = userRepository.findByKcUserId(userKcId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userKcId));

        // 3. 验证权限：只有评论作者可以编辑
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("您只能编辑自己的评论");
        }

        // 4. 检查评论状态
        if (comment.getStatus() != DocumentComment.CommentStatus.ACTIVE) {
            throw new IllegalStateException("无法编辑已删除或隐藏的评论");
        }

        // 5. 更新评论内容
        comment.setContent(content);
        comment = commentRepository.save(comment);

        log.info("Comment updated successfully: commentId={}", commentId);

        return comment;
    }

    @Override
    public void deleteComment(Long commentId, String userKcId) {
        log.info("Deleting comment: commentId={}, userKcId={}", commentId, userKcId);

        // 1. 获取评论
        DocumentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        // 2. 获取用户
        User user = userRepository.findByKcUserId(userKcId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userKcId));

        // 3. 验证权限：评论作者或团队管理员可以删除
        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isTeamAdmin = false;

        if (comment.getTeam() != null) {
            isTeamAdmin = teamMemberRepository.existsByTeamAndUserAndRoleInAndStatus(
                    comment.getTeam(),
                    user,
                    List.of(TeamMember.TeamRole.OWNER, TeamMember.TeamRole.ADMIN),
                    TeamMember.MemberStatus.ACTIVE
            );
        }

        if (!isCommentOwner && !isTeamAdmin) {
            throw new SecurityException("您没有权限删除该评论");
        }

        // 4. 软删除评论
        comment.setStatus(DocumentComment.CommentStatus.DELETED);
        commentRepository.save(comment);

        log.info("Comment deleted successfully: commentId={}", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentComment> getDocumentComments(Long documentId, Long teamId, String userKcId) {
        log.debug("Getting comments for document: documentId={}, teamId={}", documentId, teamId);

        // 1. 获取文档
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + documentId));

        // 2. 获取用户（用于权限验证）
        User user = userRepository.findByKcUserId(userKcId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userKcId));

        // 3. 根据是否指定团队获取评论
        if (teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("团队不存在: " + teamId));

            // 验证用户是否是团队成员
            boolean isMember = teamMemberRepository.existsByTeamAndUserAndStatus(
                    team, user, TeamMember.MemberStatus.ACTIVE);

            if (!isMember) {
                throw new SecurityException("您不是该团队成员，无法查看团队评论");
            }

            return commentRepository.findByDocumentAndTeamAndStatusOrderByCreatedAtDesc(
                    document, team, DocumentComment.CommentStatus.ACTIVE);
        } else {
            return commentRepository.findByDocumentAndStatusOrderByCreatedAtDesc(
                    document, DocumentComment.CommentStatus.ACTIVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentComment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentComment> getCommentReplies(Long commentId) {
        DocumentComment comment = getCommentById(commentId);
        return commentRepository.findByParentCommentAndStatusOrderByCreatedAtAsc(
                comment, DocumentComment.CommentStatus.ACTIVE);
    }
}

