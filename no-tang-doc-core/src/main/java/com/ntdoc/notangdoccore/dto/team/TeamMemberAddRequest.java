package com.ntdoc.notangdoccore.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加团队成员请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加团队成员请求")
public class TeamMemberAddRequest {

    @NotBlank(message = "用户Email不能为空")
    @Schema(description = "要添加的用户Email", example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userEmail;

    @NotNull(message = "角色不能为空")
    @Schema(description = "团队角色：ADMIN, MEMBER, VIEWER", example = "MEMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}

