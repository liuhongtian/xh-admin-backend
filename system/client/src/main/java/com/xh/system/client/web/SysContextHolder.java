package com.xh.system.client.web;

import com.xh.system.client.entity.SysUser;

/**
 * 线程变量存储器
 * 储存当前请求token,还有当前请求登录的用户，方便后续直接获取
 * sunxh 2021/2/26
 */
public class SysContextHolder {
    //系统的登录的token
    public static final ThreadLocal<String> AUTH_TOKEN = new ThreadLocal<>();
    //系统登录用户信息
    public static final ThreadLocal<SysUser> SYS_USER = new ThreadLocal<>();

    public static String getAuthToken() {
        return AUTH_TOKEN.get();
    }

    public static SysUser getSysUser() {
        return SYS_USER.get();
    }
}
