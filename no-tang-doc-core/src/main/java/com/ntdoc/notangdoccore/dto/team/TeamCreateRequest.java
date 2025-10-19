package com.ntdoc.notangdoccore.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 团队创建请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队创建请求")
public class TeamCreateRequest {

    @NotBlank(message = "团队名称不能为空")
    @Size(min = 2, max = 100, message = "团队名称长度必须在2-100字符之间")
    @Schema(description = "团队名称", example = "开发团队", required = true)
    private String name;

    @Size(max = 500, message = "团队描述不能超过500字符")
    @Schema(description = "团队描述", example = "这是一个专注于后端开发的团队")
    private String description;
}

