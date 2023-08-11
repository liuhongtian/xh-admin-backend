package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Schema(title = "系统用户")
@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity<Integer> {

    @Schema(title = "用户代码")
    private String code;
    @Schema(title = "用户名")
    private String name;
    @Schema(title = "密码")
    private String password;
    @Schema(title = "头像")
    private String avatar;
    @Schema(title = "是否启用")
    private Boolean enabled;
}
