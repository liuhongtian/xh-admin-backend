package com.xh.common.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用于承载用户登录信息
 *
 * @author sunxh 2023/3/1
 */
@Schema(description = "用于承载用户登录信息")
@Data
public class SysLoginUserInfoDto implements Serializable {

    @Schema(description = "当前登录的token")
    private String token;
    @Schema(description = "当前登录的用户信息")
    private SysUserDTO user;
    @Schema(description = "当前用户拥有的菜单权限")
    private List<SysMenuDTO> menus;
}
