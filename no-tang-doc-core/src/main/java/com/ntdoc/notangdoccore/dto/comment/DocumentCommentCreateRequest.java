package com.ntdoc.notangdoccore.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建文档评论请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建文档评论请求")
public class DocumentCommentCreateRequest {

    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID", example = "1", required = true)
    private Long documentId;

    @Schema(description = "团队ID（可选）", example = "1")
    private Long teamId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 5000, message = "评论内容长度必须在1-5000字符之间")
    @Schema(description = "评论内容", example = "这个文档写得很好！", required = true)
    private String content;

    @Schema(description = "父评论ID（用于回复评论）", example = "1")
    private Long parentCommentId;
}

