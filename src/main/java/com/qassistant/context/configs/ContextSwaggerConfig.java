package com.qassistant.context.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value="application.swagger")
public class ContextSwaggerConfig {
    @Bean
    @ConditionalOnMissingBean(value={OpenAPI.class})
    @ConditionalOnProperty(name={"application.swagger.enabled"}, havingValue="true")
    public OpenAPI contextApiInfo() {
        return new OpenAPI().info(new Info().title("Imba context Gpt").description("GPT Context").version("0.0.1"));
    }

    @Bean
    @ConditionalOnMissingBean(value={GroupedOpenApi.class})
    @ConditionalOnProperty(name={"application.swagger.enabled"}, havingValue="true")
    public GroupedOpenApi contextApi() {
        return GroupedOpenApi.builder().group("all").pathsToMatch(new String[]{"/**"}).pathsToExclude(new String[]{"/error**"}).build();
    }
}
