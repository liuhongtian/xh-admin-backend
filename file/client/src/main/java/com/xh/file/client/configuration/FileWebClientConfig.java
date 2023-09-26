package com.xh.file.client.configuration;

import com.xh.file.client.exchange.FileOperationClient;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * 文件服务 web client，跨服务调用配置
 * sunxh 2023/9/22
 */
public class FileWebClientConfig {

    @Resource
    private WebClient.Builder myWebClientBuilder;

    //服务名或者ip地址调用文件服务
    @Value("${service.file:lb://file}")
    private String fileUrl;

    public HttpServiceProxyFactory fileHttpServiceProxyFactory() {
        return HttpServiceProxyFactory.builder(
                        WebClientAdapter.forClient(myWebClientBuilder.baseUrl(fileUrl).build())
                )
                // 设置超时时间10s
                .blockTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * FileOperationService
     */
    @Bean
    public FileOperationClient getFileOperationServiceClient() {
        return fileHttpServiceProxyFactory().createClient(FileOperationClient.class);
    }
}
