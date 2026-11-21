package com.eduplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 元信息，访问 /swagger-ui.html 查看。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eduPlatformOpenApi() {
        return new OpenAPI().info(new Info()
                .title("在线教育平台 API")
                .description("讲师 / 课程 / 分类管理，前后台分离 REST 接口")
                .version("1.0.0"));
    }
}
