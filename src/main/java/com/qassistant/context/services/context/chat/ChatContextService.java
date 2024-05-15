package com.qassistant.context.services.context.chat;

import com.qassistant.context.entities.SystemMessageContext;
import java.util.List;

public interface ChatContextService<T> {
    public List<T> getChatContext(String var1);

    public void clearChatContext(String var1);

    public void setChatContext(List<T> var1, String var2);

    public void setSystemMessage(String var1, String var2);

    public void removeLastContent(String var1);

    public SystemMessageContext formatSystemMessageWithContext(String var1, String var2, String var3);

    public String completionChat(String var1, String var2);
}
