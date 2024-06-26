package com.qassistant.context.bots.slack.actions;

import com.slack.api.bolt.context.Context;
import com.slack.api.model.Message;

import java.util.List;

public interface AiAction<T> {

    void setMessageContext(List<Message> messages, String user);

    List<T> getContext(String user);

    void removeLastMessage(String user);

    String getProject(String user);

    List<String> getChatServiceResponse(
            String channelId,
            String ts,
            String text,
            String userId,
            Context ctx
    );
}