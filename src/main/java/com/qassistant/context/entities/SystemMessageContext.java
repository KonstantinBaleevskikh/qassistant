package com.qassistant.context.entities;

import java.util.List;
import java.util.Objects;

public final class SystemMessageContext {
    private final String systemMessage;
    private final List<String> ids;

    public SystemMessageContext(String systemMessage, List<String> ids) {
        this.systemMessage = systemMessage;
        this.ids = ids;
    }

    public String getSystemMessage() {
        return this.systemMessage;
    }

    public List<String> getIds() {
        return this.ids;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        SystemMessageContext systemMessageContext = (SystemMessageContext)obj;
        return Objects.equals(this.systemMessage, systemMessageContext.systemMessage) && Objects.equals(this.ids, systemMessageContext.ids);
    }

    public int hashCode() {
        return Objects.hash(this.systemMessage, this.ids);
    }

    public String toString() {
        return "SystemMessageContext[systemMessage=" + this.systemMessage + ", ids=" + this.ids + "]";
    }
}
