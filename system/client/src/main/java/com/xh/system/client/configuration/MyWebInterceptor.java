package com.xh.system.client.configuration;

import com.xh.system.client.service.SysUserLoginService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * web认证拦截器
 *
 * @author sunxh
 * 2023/2/26
 */
@Configuration
public class MyWebInterceptor implements HandlerInterceptor {

    @Resource
    private SysUserLoginService sysUserLoginService;

    /**
     * 添加拦截器，同时配置拦截规则
     */
    public void addInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns(
                        "/**"
                )
                .excludePathPatterns(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/api/system/login/login"

                ).order(2);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {

        if ("/error".equals(request.getRequestURI()) || "OPTIONS".equals(request.getMethod())) {
            return true;
        }
        return sysUserLoginService.authentication(request, response, handler);
    }
}
