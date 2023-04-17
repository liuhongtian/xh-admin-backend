package com.xh.common.jdbc.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据源配置
 * 参考文档 <a href="https://docs.spring.io/spring-boot/docs/3.0.5/reference/htmlsingle/#howto.data-access.configure-custom-datasource">...</a>
 * sunxh 2023/4/17
 */
@Configuration(proxyBeanMethods = false)
public class DataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.first")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 第一数据源，主数据源
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.first.configuration")
    public HikariDataSource firstDataSource(DataSourceProperties firstDataSourceProperties) {
        return firstDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public JdbcTemplate firstJdbcTemplate(HikariDataSource firstDataSource) {
        return new JdbcTemplate(firstDataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.second")
    public DataSourceProperties secondDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 第二数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.second.configuration")
    public HikariDataSource secondDataSource(
            @Qualifier("secondDataSourceProperties") DataSourceProperties secondDataSourceProperties) {
        return secondDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public JdbcTemplate secondJdbcTemplate(@Qualifier("secondDataSource") HikariDataSource secondDataSource) {
        return new JdbcTemplate(secondDataSource);
    }
}
