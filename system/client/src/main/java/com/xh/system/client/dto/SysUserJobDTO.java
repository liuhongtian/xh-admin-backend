package com.xh.system.client.dto;

import com.xh.system.client.entity.SysUserJob;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 存放用户岗位信息DTO
 * sunxh 2023/8/10
 */

@Schema(title = "用户或用户组岗位信息")
@Data
public class SysUserJobDTO {
    @Schema(title = "用户id或者用户组的id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;
    @Schema(title = "数据类型", allowableValues = {"1", "2"}, description = "1：用户，2：用户组", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;
    @Schema(title = "岗位信息")
    private List<SysUserJob> jobData;
}
