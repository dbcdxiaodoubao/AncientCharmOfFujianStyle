package com.ancientcharmoffujianstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc // 启用 Swagger 2（Knife4j 3.0 基于 Swagger 2）
public class Knife4jConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("古韵闽风API文档")
                        .description("基于Knife4j的接口文档")
                        .version("0.0.1")
                        .build())
                .select()
                // 扫描 Controller 包（必须是你的实际 Controller 路径）
                .apis(RequestHandlerSelectors.basePackage("com.ancientcharmoffujianstyle.controller"))
                .paths(PathSelectors.any()) // 匹配所有接口路径
                .build();
    }
}