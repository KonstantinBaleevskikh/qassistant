package com.qassistant.context.bots.slack.events;


import com.qassistant.context.bots.slack.SlackBot;
import com.qassistant.context.bots.slack.actions.AiAction;
import com.qassistant.context.bots.slack.actions.EventAction;
import com.qassistant.context.bots.slack.blocks.AnswerBlock;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.handler.builtin.BlockActionHandler;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.slack.api.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ConditionalOnBean(value = {SlackBot.class, AiAction.class})
@Component
public class RegenerateHandler implements BlockActionHandler {
    private static final Logger log = LoggerFactory.getLogger(RegenerateHandler.class);
    private final EventAction eventAction;
    private final AnswerBlock answerBlock;
    private final AiAction<org.springframework.ai.chat.messages.Message> aiAction;

    public RegenerateHandler(EventAction eventAction, AnswerBlock answerBlock, AiAction<org.springframework.ai.chat.messages.Message> aiAction) {
        this.eventAction = eventAction;
        this.answerBlock = answerBlock;
        this.aiAction = aiAction;
    }

    private List<Message> getLastUserMessages(List<Message> messages) {
        List<Message> subsequentMessages = new ArrayList<>();
        boolean foundNullAppId = false;
        for (Message message : messages) {
            if (message.getAppId() == null) {
                foundNullAppId = true;
                subsequentMessages.clear();
            } else if (foundNullAppId) {
                subsequentMessages.add(message);
            }
        }
        if (!foundNullAppId) {
            subsequentMessages.addAll(messages);
        }
        return subsequentMessages;
    }

    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) throws IOException, SlackApiException {
        BlockActionPayload payload = req.getPayload();
        String ts = payload.getMessage().getTs();
        String tsThread = payload.getMessage().getThreadTs();
        String channelId = payload.getChannel().getId();
        String userId = payload.getUser().getId();
        ConversationsRepliesResponse repliesResponse = ctx.client().conversationsReplies(ConversationsRepliesRequest.builder()
                .channel(channelId)
                .ts(tsThread)
                .build());
        List<Message> messages = repliesResponse.getMessages();
        if (repliesResponse.isOk() && messages.size() >= 2 && ts.equals(messages.get(messages.size() - 1).getTs())) {
            CompletableFuture.runAsync(() -> {
                try {
                    String lastMessageText = messages.stream()
                            .filter(mess -> mess.getAppId() == null)
                            .reduce((first, second) -> second)
                            .orElse(new Message())
                            .getText();

                    for (Message lastUserMessage : getLastUserMessages(messages)) {
                        ctx.client().chatDelete(r -> r.channel(channelId).ts(lastUserMessage.getTs()));
                    }
                    aiAction.removeLastMessage(tsThread);
                    List<String> gptResponseText = aiAction.getChatServiceResponse(
                            channelId,
                            tsThread,
                            lastMessageText,
                            userId,
                            ctx
                    );
                    for (String textMessage : gptResponseText) {
                        eventAction.sendTextIntoThread(
                                channelId,
                                tsThread,
                                textMessage,
                                answerBlock.createAiMessage(textMessage),
                                ctx
                        );
                    }
                } catch (IOException | SlackApiException e) {
                    log.error("Error processing message", e);
                }
            });
            return ctx.ack();
        } else return Response.ok();
    }
}
