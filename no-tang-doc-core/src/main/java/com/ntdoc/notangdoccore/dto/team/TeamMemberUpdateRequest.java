package com.ntdoc.notangdoccore.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新团队成员角色请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新团队成员角色请求")
public class TeamMemberUpdateRequest {

    @NotNull(message = "角色不能为空")
    @Schema(description = "新角色：ADMIN, MEMBER, VIEWER", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}

