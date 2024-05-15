package com.qassistant.context.entities;

public class SystemMessage {
    private final String engMessage;
    private final String rusMessage;

    public SystemMessage(String engMessage, String rusMessage) {
        this.engMessage = engMessage;
        this.rusMessage = rusMessage;
    }

    public String message(boolean cyrillic) {
        return cyrillic ? this.rusMessage : this.engMessage;
    }
}
