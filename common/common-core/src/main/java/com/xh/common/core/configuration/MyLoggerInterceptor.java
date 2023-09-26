package com.xh.common.core.configuration;

import cn.dev33.satoken.stp.StpUtil;
import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * Logger拦截器，主要记录请求日志
 *
 * @author sunxh 2023/2/26
 */
@Configuration
@Slf4j
public class MyLoggerInterceptor extends BaseServiceImpl implements HandlerInterceptor {

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
        //对于无法添加header但需要鉴权的请求可以将token放在参数中，手动从请求中获取token设置
        String tokenValue = request.getParameter(StpUtil.getTokenName());
        if (CommonUtil.isNotEmpty(tokenValue)) {
            StpUtil.setTokenValue(tokenValue);
        }
        return true;
    }
}
