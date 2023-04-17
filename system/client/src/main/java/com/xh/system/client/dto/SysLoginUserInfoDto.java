package com.xh.system.client.dto;

import com.xh.common.core.dto.BaseDto;
import com.xh.system.client.entity.SysMenu;
import com.xh.system.client.entity.SysUser;
import lombok.Data;

import java.util.List;

/**
 * 用于承载用户登录信息
 *
 * @author sunxh 2023/3/1
 */
@Data
public class SysLoginUserInfoDto extends BaseDto {

    //登录token
    private String token;
    //登录用户信息
    private SysUser user;
    //拥有的菜单
    private List<SysMenu> menus;
}
