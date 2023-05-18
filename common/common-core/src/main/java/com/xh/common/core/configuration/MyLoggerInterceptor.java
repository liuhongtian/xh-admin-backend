package com.xh.common.core.configuration;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.web.SysContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        if(CommonUtil.isEmpty(authToken)) authToken = request.getParameter(tokenHeaderName);
        SysContextHolder.AUTH_TOKEN.set(authToken);
        //清空当前登录用户
        SysContextHolder.SYS_USER.remove();
        Enumeration<String> headerNames = request.getHeaderNames();
        log.info("{},请求URL:{}", request.getMethod(), request.getRequestURI());
        return true;
    }
}
