package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Schema(title = "系统角色")
@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity<Integer> {

    @Schema(title = "角色名称")
    private String name;
    @Schema(title = "上级角色")
    private Integer parentId;
    @Schema(title = "启用状态")
    private Boolean enabled;
    @Transient
    @Schema(title = "上级角色名称")
    private String parentName;
    @Transient
    @Schema(title = "角色具有的权限")
    List<SysRoleMenu> roleMenus;
}
