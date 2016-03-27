package com.seventh_root.elysium.chat.exception;

public class ChatChannelMessageProcessingException extends Exception {

    public ChatChannelMessageProcessingException() {
    }

    public ChatChannelMessageProcessingException(String s) {
        super(s);
    }

    public ChatChannelMessageProcessingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ChatChannelMessageProcessingException(Throwable throwable) {
        super(throwable);
    }

    public ChatChannelMessageProcessingException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
