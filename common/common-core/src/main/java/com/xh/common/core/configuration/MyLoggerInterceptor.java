package com.xh.common.core.configuration;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.web.SysContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Enumeration;

/**
 * Logger拦截器，主要记录请求日志
 *
 * @author sunxh 2023/2/26
 */
@Configuration
public class MyLoggerInterceptor extends BaseServiceImpl implements HandlerInterceptor {
    @Value("${sys.auth.tokenHeaderName}")
    private String tokenHeaderName;

    /**
     * 添加拦截器，同时配置拦截规则
     */
    public void addInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns(
                        "/**"
                ).order(1);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        String authToken = request.getHeader(tokenHeaderName);
        SysContextHolder.AUTH_TOKEN.set(authToken);
        //清空当前登录用户
        SysContextHolder.SYS_USER.remove();
        Enumeration<String> headerNames = request.getHeaderNames();
        System.out.println(headerNames);
        return true;
    }
}
