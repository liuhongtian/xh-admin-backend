package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Schema(title = "系统用户密码修改日志")
@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPasswordLog extends BaseEntity<Integer> {
    @Schema(title = "系统用户id")
    private Integer sysUserId;
    @Schema(title = "原密码")
    private String oldPassword;
    @Schema(title = "新密码")
    private String newPassword;
}
