package com.seventh_root.elysium.api.chat;

import com.seventh_root.elysium.chat.exception.ChatChannelMessageFormattingFailureException;

public abstract class ChatChannelPipelineComponent implements Comparable<ChatChannelPipelineComponent> {

    public enum Type {

        PRE_PROCESSOR(0),
        FORMATTER(1),
        POST_PROCESSOR(2);

        private int priority;

        Type(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

    }

    @Override
    public int compareTo(ChatChannelPipelineComponent component) {
        return getType().getPriority() - component.getType().getPriority();
    }

    public abstract Type getType();

    public abstract String process(String message, ChatMessageContext context) throws ChatChannelMessageFormattingFailureException;

}
