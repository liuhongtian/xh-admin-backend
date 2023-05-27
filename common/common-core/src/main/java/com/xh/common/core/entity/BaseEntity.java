package com.xh.common.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class BaseEntity<I extends Serializable> implements Serializable {

    @Schema(title = "主键ID")
    @Id
    protected I id;
    @Schema(title = "创建时间")
    private LocalDateTime createTime;
    @Schema(title = "修改时间")
    private LocalDateTime updateTime;
    @Schema(title = "创建人")
    private Integer createBy;
    @Schema(title = "修改人")
    private Integer updateBy;
    @Schema(title = "是否已删除")
    private Boolean deleted;
}
