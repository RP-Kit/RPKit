package com.seventh_root.elysium.chat.exception;

public class ChatChannelMessageFormattingFailureException extends ChatChannelMessageProcessingException {

    public ChatChannelMessageFormattingFailureException() {
    }

    public ChatChannelMessageFormattingFailureException(String s) {
        super(s);
    }

    public ChatChannelMessageFormattingFailureException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ChatChannelMessageFormattingFailureException(Throwable throwable) {
        super(throwable);
    }

    public ChatChannelMessageFormattingFailureException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
