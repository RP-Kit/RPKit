package com.seventh_root.elysium.chat.exception;

public class ChatChannelMessagePreProcessingException extends ChatChannelMessageProcessingException {
    public ChatChannelMessagePreProcessingException() {
    }

    public ChatChannelMessagePreProcessingException(String s) {
        super(s);
    }

    public ChatChannelMessagePreProcessingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ChatChannelMessagePreProcessingException(Throwable throwable) {
        super(throwable);
    }

    public ChatChannelMessagePreProcessingException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
