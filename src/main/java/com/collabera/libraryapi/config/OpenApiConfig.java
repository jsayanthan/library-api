package com.collabera.libraryapi.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    GroupedOpenApi libraryApi() {
        return GroupedOpenApi.builder()
                .group("library")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(openApi -> openApi.setInfo(new Info()
                        .title("Collabera Library API")
                        .description("Collabera library management API")
                        .version("1.0.0")
                        .license(new License().name("MIT"))))
                .build();
    }
}
