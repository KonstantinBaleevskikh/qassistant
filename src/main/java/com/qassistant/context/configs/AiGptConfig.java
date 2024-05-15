package com.qassistant.context.configs;

import java.util.Optional;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value="application.gpt")
public class AiGptConfig {
    private String secretKey;
    private String embeddingsModel;
    private String chatModel;
    private Integer maxTokens;
    private Integer chatContextLength;

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return Optional.ofNullable(this.secretKey).orElseThrow(() -> new RuntimeException("Set secret key in config"));
    }

    public String getEmbeddingsModel() {
        return Optional.ofNullable(this.embeddingsModel).orElse("text-embedding-3-large");
    }

    public void setEmbeddingsModel(String embeddingsModel) {
        this.embeddingsModel = embeddingsModel;
    }

    public String getChatModel() {
        return Optional.ofNullable(this.chatModel).orElse("gpt-4-turbo-preview");
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public Integer getMaxTokens() {
        return Optional.ofNullable(this.maxTokens).orElse(4096);
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getChatContextLength() {
        return Optional.ofNullable(this.chatContextLength).orElse(7000000);
    }

    public void setChatContextLength(Integer chatContextLength) {
        this.chatContextLength = chatContextLength;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiApi getAiApi() {
        return new OpenAiApi(this.secretKey);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiImageApi getApiImage() {
        return new OpenAiImageApi(this.secretKey);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiEmbeddingClient getEmbeddingClient(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingClient(openAiApi, MetadataMode.EMBED, OpenAiEmbeddingOptions.builder().withModel(this.embeddingsModel).withUser("user").build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiChatClient getChatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi, OpenAiChatOptions.builder().withModel(this.chatModel).withMaxTokens(this.maxTokens).withUser("user").build());
    }
}
