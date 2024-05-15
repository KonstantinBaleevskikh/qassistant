package com.qassistant.context.entities.prompts;

public class PromptMessage {
    private ChatMessageRole role;
    private String content;

    public PromptMessage() {
    }

    public PromptMessage(ChatMessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return this.role.value();
    }

    public void setRole(String role) {
        this.role = ChatMessageRole.valueOf(role.toUpperCase());
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
