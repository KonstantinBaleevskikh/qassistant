package com.qassistant.context.configs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ConfigurationProperties(value="application.context")
public class ContextConfig {
    private List<String> ignorePatterns;
    private Integer sourceMaxToken;
    private Integer contextEntries;

    public List<String> getIgnorePatterns() {
        return Optional.ofNullable(this.ignorePatterns).orElse(Collections.emptyList());
    }

    public void setIgnorePatterns(List<String> ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    public int getSourceMaxToken() {
        return Optional.ofNullable(this.sourceMaxToken).orElse(1000);
    }

    public void setSourceMaxToken(int sourceMaxToken) {
        this.sourceMaxToken = sourceMaxToken;
    }

    public Integer getContextEntries() {
        return Optional.ofNullable(this.contextEntries).orElse(10);
    }

    public void setContextEntries(Integer contextEntries) {
        this.contextEntries = contextEntries;
    }
}
