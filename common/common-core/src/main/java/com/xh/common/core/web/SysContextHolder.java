package com.xh.common.core.web;

import com.xh.common.core.dto.SysUserDTO;

/**
 * 线程变量存储器
 * 储存当前请求token,还有当前请求登录的用户，方便后续直接获取
 * sunxh 2021/2/26
 */
public class SysContextHolder {
    //系统的登录的token
    public static final ThreadLocal<String> AUTH_TOKEN = new ThreadLocal<>();
    //系统登录用户信息
    public static final ThreadLocal<SysUserDTO> SYS_USER = new ThreadLocal<>();

    public static String getAuthToken() {
        return AUTH_TOKEN.get();
    }

    public static SysUserDTO getSysUser() {
        return SYS_USER.get();
    }
}
