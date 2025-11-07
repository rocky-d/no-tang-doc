package com.ntdoc.notangdoccore.dto.comment;

import com.ntdoc.notangdoccore.entity.DocumentComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档评论响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档评论响应")
public class DocumentCommentResponse {

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    @Schema(description = "文档标题", example = "项目需求文档.pdf")
    private String documentTitle;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "用户邮箱", example = "john@example.com")
    private String userEmail;

    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    @Schema(description = "团队名称", example = "开发团队")
    private String teamName;

    @Schema(description = "评论内容", example = "这个文档写得很好！")
    private String content;

    @Schema(description = "父评论ID", example = "1")
    private Long parentCommentId;

    @Schema(description = "评论状态", example = "ACTIVE")
    private String status;

    @Schema(description = "回复数量", example = "3")
    private int replyCount;

    @Schema(description = "子评论列表")
    private List<DocumentCommentResponse> replies;

    @Schema(description = "创建时间")
    private Instant createdAt;

    @Schema(description = "更新时间")
    private Instant updatedAt;

    /**
     * 从实体转换为 DTO
     */
    public static DocumentCommentResponse fromEntity(DocumentComment comment) {
        if (comment == null) {
            return null;
        }

        return DocumentCommentResponse.builder()
                .id(comment.getId())
                .documentId(comment.getDocument().getId())
                .documentTitle(comment.getDocument().getOriginalFilename())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userEmail(comment.getUser().getEmail())
                .teamId(comment.getTeam() != null ? comment.getTeam().getId() : null)
                .teamName(comment.getTeam() != null ? comment.getTeam().getName() : null)
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .status(comment.getStatus().name())
                .replyCount(0)
                .replies(new ArrayList<>())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * 从实体列表转换为 DTO 列表
     */
    public static List<DocumentCommentResponse> fromEntities(List<DocumentComment> comments) {
        if (comments == null) {
            return new ArrayList<>();
        }
        return comments.stream()
                .map(DocumentCommentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

