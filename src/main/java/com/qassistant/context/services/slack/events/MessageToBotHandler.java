package com.qassistant.context.services.slack.events;

import com.qassistant.context.services.slack.actions.AiAction;
import com.qassistant.context.services.slack.blocks.AnswerBlock;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnBean(AiAction.class)
public class MessageToBotHandler implements BoltEventHandler<MessageEvent> {
    private static final Logger log = LoggerFactory.getLogger(MessageToBotHandler.class);
    private final AnswerBlock block;
    private final AiAction<Message> aiAction;

    public MessageToBotHandler(AnswerBlock block, AiAction<Message> aiAction) {
        this.block = block;
        this.aiAction = aiAction;
    }

    @Override
    public Response apply(EventsApiPayload<MessageEvent> payload, EventContext ctx) {
        MessageEvent event = payload.getEvent();
        if ("im".equals(event.getChannelType())) {
            String text = event.getText();
            CompletableFuture.runAsync(() -> {
                try {
                    boolean newMessage = event.getThreadTs() == null;
                    if (!newMessage && aiAction.getContext(event.getThreadTs()).isEmpty()) {
                        ConversationsRepliesResponse repliesResponse = ctx.client().conversationsReplies(ConversationsRepliesRequest.builder()
                                .channel(event.getChannel())
                                .ts(event.getThreadTs())
                                .build());
                        aiAction.setMessageContext(repliesResponse.getMessages(), event.getThreadTs());
                    }
                    List<String> gptResponseText = aiAction.getChatServiceResponse(
                            event.getChannel(),
                            newMessage ? event.getTs() : event.getThreadTs(),
                            text,
                            ctx
                    );
                    for (String textMessage : gptResponseText) {
                        ChatPostMessageResponse response = ctx.client().chatPostMessage(ChatPostMessageRequest.builder()
                                .channel(event.getChannel())
                                .threadTs(event.getTs())
                                .text(textMessage)
                                .blocks(block.createAiMessage(textMessage))
                                .build());
                        if (!response.isOk()) {
                            log.error("Error posting message: {}", response.getError());
                        }
                    }

                } catch (IOException | SlackApiException e) {
                    log.error("Error processing message", e);
                }
            });
            return ctx.ack();
        } else {
            return Response.ok();
        }
    }

}
