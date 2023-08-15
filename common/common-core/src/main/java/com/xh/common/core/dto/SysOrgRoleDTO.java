package com.xh.common.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存放系统登录的用户信息DTO
 *
 * @author sunxh 2023/3/1
 */

@Data
public class SysOrgRoleDTO extends BaseDTO<Integer> {
    @Schema(title = "数据类型", allowableValues = {"1", "2"}, description = "1：用户，2：用户组")
    private Integer type;
    @Schema(title = "用户id或者用户组的id")
    private Integer userId;
    @Schema(title = "机构id")
    private Integer sysOrgId;
    @Schema(title = "角色id")
    private Integer sysRoleId;
    @Schema(title = "是否启用1：是，0：否")
    private Boolean enabled;
    @Schema(title = "机构代码")
    private String orgCode;
    @Schema(title = "机构名称")
    private String orgName;
    @Schema(title = "角色名称")
    private String roleName;
}
