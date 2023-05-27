package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Table
@Data
@Schema(title = "数据字典类型")
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity<Integer> {
    @Schema(title = "字典类型名称")
    private String name;
    @Schema(title = "是否可修改")
    private Boolean modifiable;
}
