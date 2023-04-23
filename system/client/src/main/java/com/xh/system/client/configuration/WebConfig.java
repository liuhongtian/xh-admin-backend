package com.xh.system.client.configuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * spring mvc配置类
 * sunxh 2023/2/26
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private MyWebInterceptor myWebInterceptor;

    /**
     * 请求拦截器，鉴权
     */
    @Override
    public void addInterceptors(@Nonnull InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        myWebInterceptor.addInterceptor(registry);
    }
}
