package com.qassistant.context.entities.prompts;

import java.util.List;

public class NewPrompt {
    private List<PromptMessage> messages;

    public List<PromptMessage> getMessages() {
        return this.messages;
    }

    public void setMessages(List<PromptMessage> messages) {
        this.messages = messages;
    }
}
