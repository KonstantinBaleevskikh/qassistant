package com.qassistant.context.services.slack.actions;


import com.qassistant.context.entities.SystemMessageContext;
import com.qassistant.context.services.context.systemMessage.ContextFormater;
import com.qassistant.context.services.context.chat.ChatContextService;
import com.qassistant.context.utils.TextUtils;
import com.slack.api.bolt.context.Context;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.qassistant.context.services.slack.styles.StyleWithContext.CODE_CONTEXT;


@Component
@ConditionalOnBean(ChatContextService.class)
public class GptAction implements AiAction<Message> {
    private static final Logger log = LoggerFactory.getLogger(EventAction.class);
    private final ChatContextService<Message> chatContextService;
    private final ContextFormater contextFormatter;
    private final EventAction eventAction;
    private static final String typingMessage = "⏳ Typing message...";
    private static final int maxSymbols = 2800;

    public GptAction(
            ChatContextService<Message> chatContextService,
            ContextFormater contextFormatter,
            EventAction eventAction
    ) {
        this.chatContextService = chatContextService;
        this.contextFormatter = contextFormatter;
        this.eventAction = eventAction;
    }

    @Override
    public void setMessageContext(List<com.slack.api.model.Message> messages, String user) {
        List<Message> chatMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            chatMessages.add(i % 2 == 0
                    ? new UserMessage(messages.get(i).getText())
                    : new AssistantMessage(messages.get(i).getText())
            );
        }
        chatContextService.setChatContext(chatMessages, user);
    }

    @Override
    public List<Message> getContext(String user) {
        return chatContextService.getChatContext(user);
    }

    @Override
    public void removeLastMessage(String user) {
        chatContextService.removeLastContent(user);
    }


    @Override
    public List<String> getChatServiceResponse(
            String channelId,
            String ts,
            String text,
            Context ctx
    ) {
        try {
            ChatPostMessageResponse typingResponse = eventAction.sendTextIntoThread(channelId, ts, typingMessage, null, ctx);
            String chatText;
            try {
                SystemMessageContext systemMessageContext = contextFormatter.formatContextToSystemMessage(
                        project,
                        CODE_CONTEXT.getSystemMessage(),
                        text);
                chatContextService.setSystemMessage(systemMessageContext.getSystemMessage(), String.valueOf(ts));
                chatText = chatContextService.completionChat(text, String.valueOf(ts));
            } catch (Exception e) {
                log.error("", e);
                chatText = e.getMessage();
            }
            ctx.client().chatDelete(r -> r.channel(channelId).ts(typingResponse.getTs()));
            return TextUtils.splitTextWithMarkdown(chatText, maxSymbols);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
