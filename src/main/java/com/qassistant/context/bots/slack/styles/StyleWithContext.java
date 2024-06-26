package com.qassistant.context.bots.slack.styles;


public enum StyleWithContext {

    CODE_CONTEXT(
            """
                     Context sections:
                    
                     %s
                    
                     "'
                    Use the context provided to answer the question below as accurately as possible.
                    When generating code, use the same code and style as in the given context.
                    """
    );

    private final String systemMessage;

    StyleWithContext(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public String getSystemMessage() {
        return systemMessage;
    }
}
