package com.qassistant.context.configs;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix="application.github", name={"token", "userName"})
@ConfigurationProperties(value="application.github")
public class ContextGithubConfig {
    private String userName;
    private String token;

    public String getUserName() {
        return Optional.ofNullable(this.userName).orElseThrow(() -> new RuntimeException("Set github username in config"));
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return Optional.ofNullable(this.token).orElseThrow(() -> new RuntimeException("Set github token in config"));
    }

    public void setToken(String token) {
        this.token = token;
    }
}
