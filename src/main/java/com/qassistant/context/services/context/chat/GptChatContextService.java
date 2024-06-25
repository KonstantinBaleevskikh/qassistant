package com.qassistant.context.services.context.chat;

import com.qassistant.context.db.service.DbService;
import com.qassistant.context.entities.Context;
import com.qassistant.context.configs.AiGptConfig;
import com.qassistant.context.configs.ContextConfig;
import com.qassistant.context.entities.SystemMessageContext;
import com.qassistant.context.utils.TextUtils;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(prefix = "application.gpt", name = "embeddingsModel")
@Qualifier("default")
@ConditionalOnBean(DbService.class)
public class GptChatContextService implements ChatContextService<Message> {
    private final Map<String, List<Message>> userChatContexts = new ConcurrentHashMap<>();
    private final OpenAiChatClient chatClient;
    private final AiGptConfig gptConfig;
    private final DbService dbService;
    private final ContextConfig contextConfig;

    public GptChatContextService(OpenAiChatClient chatClient, AiGptConfig gptConfig, DbService dbService, ContextConfig contextConfig){
        this.chatClient = chatClient;
        this.gptConfig = gptConfig;
        this.dbService = dbService;
        this.contextConfig = contextConfig;
    }

    @Override
    public List<Message> getChatContext(String userId) {
        return userChatContexts.computeIfAbsent(userId, k -> new ArrayList<>());
    }

    @Override
    public void clearChatContext(String userId) {
        userChatContexts.getOrDefault(userId, new ArrayList<>()).clear();
    }

    @Override
    public void setChatContext(List<Message> messages, String userId) {
        userChatContexts.put(userId, messages);
    }

    @Override
    public void setSystemMessage(String message, String userId) {
        List<Message> messages = getChatContext(userId);
        if (messages.isEmpty()) {
            messages.add(new SystemMessage(message));
        }
    }

    @Override
    public void removeLastContent(String userId) {
        List<Message> messages = getChatContext(userId);
        if (!messages.isEmpty()) {
            messages.remove(messages.size() - 1);
        }
    }

    @Override
    public SystemMessageContext formatSystemMessageWithContext(String projectId, String systemMessage, String prompt) {
        List<Context> contexts = dbService.findContext(projectId, prompt, contextConfig.getContextEntries());
        if (contexts.isEmpty()) {
            throw new RuntimeException("Context is empty for the given prompt");
        }
        String combinedMessages = String.join("\n", contexts.stream().map(Context::getContent).toList());
        return new SystemMessageContext(String.format(systemMessage, combinedMessages), contexts.stream().map(Context::getId).toList());
    }

    @Override
    public String completionChat(String prompt, String userId) {
        List<Message> messages = getChatContext(userId);
        if (messages.isEmpty()) {
            throw new RuntimeException("Chat context not set. Please set the system message first.");
        }
        messages.add(new UserMessage(prompt));

        try {
            return generateChatCompletion(messages, TextUtils.containsCyrillic(prompt));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String generateChatCompletion(List<Message> messages, boolean containsCyrillic) {
        String finishReason;
        StringBuilder completion = new StringBuilder();
        String continuePrompt = containsCyrillic ? "продолжай пожалуйста" : "continue please";

        do {
            ChatResponse response = chatClient.call(new Prompt(messages));
            Generation result = response.getResult();
            AssistantMessage output = result.getOutput();
            finishReason = result.getMetadata().getFinishReason();
            messages.add(output);
            completion.append(output.getContent()).append(" ");

            if (!"stop".equalsIgnoreCase(finishReason)) {
                messages.add(new UserMessage(continuePrompt));
            }
        } while (!"stop".equalsIgnoreCase(finishReason));

        return completion.toString().trim();
    }

    private String summarizeIfExceedsMaxLength(List<Message> messages, boolean containsCyrillic) {
        int totalLength = messages.stream()
                .mapToInt(message -> message.getContent() != null ? message.getContent().length() : 0)
                .sum();

        // Check if the total content length exceeds the configured maximum chat context length
        if (totalLength > gptConfig.getChatContextLength()) {
            // Find the first system message to keep as context, or default to an empty system message
            Message firstSystemMessage = messages.stream()
                    .filter(message -> message.getMessageType() == MessageType.SYSTEM)
                    .findFirst()
                    .orElse(new SystemMessage(""));

            // Prepare to summarize the conversation if length is exceeded
            String summaryPrompt = containsCyrillic ? "Подведи итог" : "Sum it up";
            messages.add(new UserMessage(summaryPrompt));

            // Call the chat service to generate a summary
            ChatResponse chatResponse = chatClient.call(new Prompt(messages));
            AssistantMessage summaryMessage = chatResponse.getResult().getOutput();

            // Clear the existing messages and add the context and the summary
            messages.clear();
            messages.add(firstSystemMessage);
            messages.add(summaryMessage);

            return summaryMessage.getContent();
        }
        return null; // Return null if no summary is necessary
    }
}