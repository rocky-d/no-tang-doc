package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.DocumentComment;

import java.util.List;

/**
 * 文档评论服务接口
 */
public interface DocumentCommentService {

    /**
     * 创建文档评论
     *
     * @param documentId 文档ID
     * @param teamId 团队ID（可选）
     * @param content 评论内容
     * @param parentCommentId 父评论ID（可选）
     * @param userKcId 用户Keycloak ID
     * @return 创建的评论
     */
    DocumentComment createComment(Long documentId, Long teamId, String content,
                                  Long parentCommentId, String userKcId);

    /**
     * 更新文档评论
     *
     * @param commentId 评论ID
     * @param content 新的评论内容
     * @param userKcId 用户Keycloak ID
     * @return 更新后的评论
     */
    DocumentComment updateComment(Long commentId, String content, String userKcId);

    /**
     * 删除文档评论
     *
     * @param commentId 评论ID
     * @param userKcId 用户Keycloak ID
     */
    void deleteComment(Long commentId, String userKcId);

    /**
     * 获取文档的所有评论
     *
     * @param documentId 文档ID
     * @param teamId 团队ID（可选）
     * @param userKcId 用户Keycloak ID
     * @return 评论列表
     */
    List<DocumentComment> getDocumentComments(Long documentId, Long teamId, String userKcId);

    /**
     * 获取指定评论
     *
     * @param commentId 评论ID
     * @return 评论
     */
    DocumentComment getCommentById(Long commentId);

    /**
     * 获取评论的所有回复
     *
     * @param commentId 评论ID
     * @return 回复列表
     */
    List<DocumentComment> getCommentReplies(Long commentId);
}

