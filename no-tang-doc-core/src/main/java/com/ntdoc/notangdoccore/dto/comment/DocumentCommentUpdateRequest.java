package com.ntdoc.notangdoccore.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新文档评论请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新文档评论请求")
public class DocumentCommentUpdateRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 5000, message = "评论内容长度必须在1-5000字符之间")
    @Schema(description = "评论内容", example = "更新后的评论内容", required = true)
    private String content;
}

