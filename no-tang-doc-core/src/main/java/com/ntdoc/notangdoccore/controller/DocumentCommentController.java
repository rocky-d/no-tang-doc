package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.comment.*;
import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.DocumentComment;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.service.DocumentCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents/{documentId}/comments")
@RequiredArgsConstructor
@Tag(name = "文档评论管理", description = "团队成员对文档的评论和讨论")
public class DocumentCommentController {

    private final DocumentCommentService commentService;
    private final DocumentRepository documentRepository;

    /**
     * 创建文档评论
     */
    @PostMapping
    @Operation(summary = "创建文档评论", description = "团队成员可以对文档进行评论")
    public ResponseEntity<ApiResponse<DocumentCommentResponse>> createComment(
            @Parameter(description = "文档ID", required = true)
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentCommentCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to create comment: documentId={}, teamId={}",
                    documentId, request.getTeamId());

            String userKcId = jwt.getClaimAsString("sub");

            DocumentComment comment = commentService.createComment(
                    documentId,
                    request.getTeamId(),
                    request.getContent(),
                    request.getParentCommentId(),
                    userKcId
            );

            DocumentCommentResponse response = DocumentCommentResponse.fromEntity(comment);

            log.info("Comment created successfully: commentId={}, documentId={}",
                    comment.getId(), documentId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("评论创建成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for creating comment: documentId={}", documentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Invalid request for creating comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create comment: documentId={}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建评论失败: " + e.getMessage()));
        }
    }

    /**
     * 获取文档的所有评论
     */
    @GetMapping
    @Operation(summary = "获取文档评论列表", description = "获取指定文档的所有评论")
    public ResponseEntity<ApiResponse<DocumentCommentListResponse>> getDocumentComments(
            @Parameter(description = "文档ID", required = true)
            @PathVariable Long documentId,
            @Parameter(description = "团队ID（可选）")
            @RequestParam(value = "teamId", required = false) Long teamId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to get comments: documentId={}, teamId={}",
                    documentId, teamId);

            String userKcId = jwt.getClaimAsString("sub");

            List<DocumentComment> comments = commentService.getDocumentComments(
                    documentId, teamId, userKcId);

            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("文档不存在"));

            DocumentCommentListResponse response = DocumentCommentListResponse.builder()
                    .documentId(documentId)
                    .documentTitle(document.getOriginalFilename())
                    .comments(DocumentCommentResponse.fromEntities(comments))
                    .totalComments(comments.size())
                    .build();

            log.info("Retrieved {} comments for document: documentId={}",
                    comments.size(), documentId);

            return ResponseEntity.ok(ApiResponse.success("获取评论列表成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for getting comments: documentId={}", documentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get comments: documentId={}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取评论列表失败: " + e.getMessage()));
        }
    }

    /**
     * 更新评论
     */
    @PutMapping("/{commentId}")
    @Operation(summary = "更新评论", description = "评论作者可以更新自己的评论")
    public ResponseEntity<ApiResponse<DocumentCommentResponse>> updateComment(
            @Parameter(description = "文档ID", required = true)
            @PathVariable Long documentId,
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId,
            @Valid @RequestBody DocumentCommentUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to update comment: documentId={}, commentId={}",
                    documentId, commentId);

            String userKcId = jwt.getClaimAsString("sub");

            DocumentComment comment = commentService.updateComment(
                    commentId, request.getContent(), userKcId);

            DocumentCommentResponse response = DocumentCommentResponse.fromEntity(comment);

            log.info("Comment updated successfully: commentId={}", commentId);

            return ResponseEntity.ok(ApiResponse.success("评论更新成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for updating comment: commentId={}", commentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Invalid request for updating comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update comment: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新评论失败: " + e.getMessage()));
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论", description = "评论作者或团队管理员可以删除评论")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "文档ID", required = true)
            @PathVariable Long documentId,
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received request to delete comment: documentId={}, commentId={}",
                    documentId, commentId);

            String userKcId = jwt.getClaimAsString("sub");

            commentService.deleteComment(commentId, userKcId);

            log.info("Comment deleted successfully: commentId={}", commentId);

            return ResponseEntity.ok(ApiResponse.success("评论删除成功"));

        } catch (SecurityException e) {
            log.warn("Access denied for deleting comment: commentId={}", commentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to delete comment: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除评论失败: " + e.getMessage()));
        }
    }

    /**
     * 获取评论的回复
     */
    @GetMapping("/{commentId}/replies")
    @Operation(summary = "获取评论回复", description = "获取指定评论的所有回复")
    public ResponseEntity<ApiResponse<List<DocumentCommentResponse>>> getCommentReplies(
            @Parameter(description = "文档ID", required = true)
            @PathVariable Long documentId,
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {

        try {
            log.info("Received request to get comment replies: documentId={}, commentId={}",
                    documentId, commentId);

            List<DocumentComment> replies = commentService.getCommentReplies(commentId);

            List<DocumentCommentResponse> response = DocumentCommentResponse.fromEntities(replies);

            log.info("Retrieved {} replies for comment: commentId={}", replies.size(), commentId);

            return ResponseEntity.ok(ApiResponse.success("获取回复列表成功", response));

        } catch (Exception e) {
            log.error("Failed to get comment replies: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取回复列表失败: " + e.getMessage()));
        }
    }
}

