package com.ntdoc.notangdoccore.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文档评论列表响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档评论列表响应")
public class DocumentCommentListResponse {

    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    @Schema(description = "文档标题", example = "项目需求文档.pdf")
    private String documentTitle;

    @Schema(description = "评论列表")
    private List<DocumentCommentResponse> comments;

    @Schema(description = "评论总数", example = "10")
    private int totalComments;
}

