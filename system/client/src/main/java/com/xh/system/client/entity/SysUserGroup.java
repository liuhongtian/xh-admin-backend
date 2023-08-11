package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Schema(title = "系统用户组")
@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserGroup extends BaseEntity<Integer> {

    @Schema(title = "用户组名")
    private String name;
    @Schema(title = "是否启用")
    private Boolean enabled;

    @Schema(title = "岗位信息")
    @Transient
    private List<SysUserJob> jobData;
    @Schema(title = "用户组成员")
    @Transient
    private List<SysUserGroupMember> memberData;
}
