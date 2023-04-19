package com.xh.common.core.dto;

import lombok.Data;

/**
 * 存放系统登录的用户信息DTO
 *
 * @author sunxh 2023/3/1
 */
@Data
public class SysUserDto extends BaseDto<Integer> {
    private String code;
    private String name;
    private String password;
    private String avatar;
    private Boolean enabled;
}
