package com.xh.common.swagger.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * 配置swagger
 * sunxh 2023/10/7
 */
public class SpringDocConfig {
    @Value("${sys.auth.tokenHeaderName}")
    private String tokenHeaderName;

    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .specVersion(SpecVersion.V31)
                .info(new Info().title("晓寒管理系统API").version("v1.0.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("文档首页")
                        .url("https://")
                )
                //需要授权
                .addSecurityItem(
                        new SecurityRequirement().addList(tokenHeaderName)
                )
                .schemaRequirement(tokenHeaderName,
                        new SecurityScheme()
                                .name(tokenHeaderName)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER));
    }
}
