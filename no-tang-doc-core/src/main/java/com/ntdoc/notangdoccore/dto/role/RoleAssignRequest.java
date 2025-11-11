package com.ntdoc.notangdoccore.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色分配请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignRequest {

    /**
     * 角色名称 (USER/ADMIN)
     */
    @NotBlank(message = "角色名称不能为空")
    @Pattern(regexp = "USER|ADMIN", message = "角色名称必须是USER或ADMIN")
    private String roleName;
}

