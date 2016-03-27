package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline;

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent;
import com.seventh_root.elysium.api.chat.ChatMessageContext;

import java.io.IOException;

import static com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.POST_PROCESSOR;

public class BukkitLogChatChannelPipelineComponent extends ChatChannelPipelineComponent {

    @Override
    public Type getType() {
        return POST_PROCESSOR;
    }

    @Override
    public String process(String message, ChatMessageContext context) {
        try {
            context.getChatChannel().log(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return message;
    }

}
