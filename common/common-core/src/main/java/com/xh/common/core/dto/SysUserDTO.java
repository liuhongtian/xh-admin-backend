package com.xh.common.core.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 存放系统登录的用户信息DTO
 *
 * @author sunxh 2023/3/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserDTO extends BaseDTO<Integer> {
    private String code;
    private String name;
    private String avatar;
    private Boolean enabled;
}
