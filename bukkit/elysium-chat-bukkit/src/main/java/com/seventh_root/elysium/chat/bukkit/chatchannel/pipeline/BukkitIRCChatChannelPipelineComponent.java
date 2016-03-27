package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline;

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent;
import com.seventh_root.elysium.api.chat.ChatMessageContext;

import static com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.POST_PROCESSOR;

public class BukkitIRCChatChannelPipelineComponent extends ChatChannelPipelineComponent {

    private String ircChannel;
    private boolean whitelist;

    public BukkitIRCChatChannelPipelineComponent(String ircChannel, boolean whitelist) {
        this.ircChannel = ircChannel;
        this.whitelist = whitelist;
    }

    @Override
    public Type getType() {
        return POST_PROCESSOR;
    }

    @Override
    public String process(String message, ChatMessageContext context) {
        return message;
    }

    public String getIRCChannel() {
        return ircChannel;
    }

    public void setIRCChannel(String ircChannel) {
        this.ircChannel = ircChannel;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

}
