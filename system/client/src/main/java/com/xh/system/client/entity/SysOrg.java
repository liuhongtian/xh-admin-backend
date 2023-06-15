package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Schema(title = "系统机构")
@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOrg extends BaseEntity<Integer> {

    @Schema(title = "机构代码")
    private String code;
    @Schema(title = "机构名称")
    private String name;
    @Schema(title = "上级机构")
    private String parentId;
    @Schema(title = "启用状态")
    private Boolean enabled;
    @Schema(title = "上级机构名称")
    @Transient
    private String parentName;
}
