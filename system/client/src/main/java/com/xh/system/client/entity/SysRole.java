package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Schema(title = "系统角色")
@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity<Integer> {

    @Schema(title = "角色代码")
    private String code;
    @Schema(title = "角色名称")
    private String name;
    @Schema(title = "上级角色")
    private String parentId;
    @Schema(title = "启用状态")
    private Boolean enabled;
}
