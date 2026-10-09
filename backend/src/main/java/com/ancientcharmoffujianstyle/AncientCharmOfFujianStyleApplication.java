package com.ancientcharmoffujianstyle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 福建风格古建筑项目启动类
 *
 * SpringBootApplication 注解包含了以下三个核心注解：
 * 1. @Configuration: 将此类标记为Spring的一个配置类。
 * 2. @EnableAutoConfiguration: 启用Spring Boot的自动配置机制。
 * 3. @ComponentScan: 扫描当前包及其子包，寻找带有 @Component, @Service, @Controller 等注解的类。
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.ancientcharmoffujianstyle.mapper") // 明确扫描MyBatis的Mapper接口
public class AncientCharmOfFujianStyleApplication {

    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(AncientCharmOfFujianStyleApplication.class, args);

        // 启动成功后，在控制台打印项目访问信息
        System.out.println("==================================================");
        System.out.println(">>>          福建风格古建筑项目启动成功!          <<<");
        System.out.println(">>>          API文档: http://localhost:8080/AncientCharmOfFujianStyle/doc.html <<<");
        System.out.println("==================================================");
    }
}