package com.ntdoc.notangdoccore.dto.role;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量角色分配请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchRoleAssignRequest {

    /**
     * 角色名称列表
     */
    @NotEmpty(message = "角色列表不能为空")
    private List<String> roleNames;
}

