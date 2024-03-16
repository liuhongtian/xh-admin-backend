package com.xh.common.core.web;

import com.xh.common.core.entity.SysLog;

/**
 * 线程变量：记录请求参数，响应参数等日志信息
 * 最后保存日志
 *
 * @author sunxh 2024/5/8
 */
public class MyContext {
    public static final ThreadLocal<SysLog> sysLogContext = new ThreadLocal<>();

    public static SysLog getSysLog() {
        return getSysLog(false);
    }

    public static SysLog getSysLog(Boolean newer) {
        SysLog sysLog = sysLogContext.get();
        if (Boolean.TRUE.equals(newer) || sysLog == null) {
            sysLog = new SysLog();
            sysLogContext.set(sysLog);
        }
        return sysLog;
    }

    public static void setSysLog(SysLog sysLog) {
        sysLogContext.set(sysLog);
    }
}
