package com.qassistant.context.services.context.systemMessage;

import com.qassistant.context.entities.SystemMessageContext;

public interface ContextFormater {
    SystemMessageContext formatContextToSystemMessage(String project, String formatString, String prompt);
}
