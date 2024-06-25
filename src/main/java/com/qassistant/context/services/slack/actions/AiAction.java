package com.qassistant.context.services.slack.actions;

import com.slack.api.bolt.context.Context;
import com.slack.api.model.Message;

import java.util.List;

public interface AiAction<T> {
    String project = "Qassistant";

    void setMessageContext(List<Message> messages, String user);

    List<T> getContext(String user);

    void removeLastMessage(String user);

    List<String> getChatServiceResponse(
            String channelId,
            String ts,
            String text,
            Context ctx
    );
}