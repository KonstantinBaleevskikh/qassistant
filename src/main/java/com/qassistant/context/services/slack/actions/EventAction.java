package com.qassistant.context.services.slack.actions;

import com.slack.api.bolt.context.Context;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.reactions.ReactionsAddRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.reactions.ReactionsAddResponse;
import com.slack.api.model.block.LayoutBlock;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class EventAction {

    public ChatPostMessageResponse sendTextIntoThread(String channelId, String ts, String text, List<LayoutBlock> blocks, Context ctx) throws SlackApiException, IOException {
        ChatPostMessageRequest messageRequest = ChatPostMessageRequest.builder()
                .channel(channelId)
                .threadTs(ts)
                .text(text)
                .blocks(blocks)
                .build();
        ChatPostMessageResponse response = ctx.client().chatPostMessage(messageRequest);
        if (response.isOk()) return response;
        else return null;
    }

    public Response sendReaction(String channelId, String ts, String reaction, Context ctx) throws SlackApiException, IOException {
        ReactionsAddRequest reactionsAddRequest = ReactionsAddRequest.builder()
                .channel(channelId)
                .timestamp(ts)
                .name(reaction)
                .build();
        ReactionsAddResponse response = ctx.client().reactionsAdd(reactionsAddRequest);
        if (response.isOk()) return ctx.ack();
        else return Response.builder().statusCode(500).body(response.getError()).build();
    }
}
