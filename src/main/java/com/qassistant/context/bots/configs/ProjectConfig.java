package com.qassistant.context.bots.configs;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProjectConfig {
    private final ConcurrentHashMap<String, String> userProjectMap = new ConcurrentHashMap<>();

    public void setProjectForUser(String userId, String project) {
        userProjectMap.put(userId, project);
    }

    public String getProjectForUser(String userId) {
        return userProjectMap.getOrDefault(userId, "Qassistant");
    }
}