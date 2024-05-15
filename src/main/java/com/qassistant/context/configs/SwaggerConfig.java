package com.qassistant.context.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Imba Gpt")
                        .description("GPT Interaction")
                        .version("0.0.1")
                );
    }

    @Bean
    @Primary
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder().group("all")
                .pathsToMatch("/**")
                .pathsToExclude("/error**")
                .build();
    }
}
