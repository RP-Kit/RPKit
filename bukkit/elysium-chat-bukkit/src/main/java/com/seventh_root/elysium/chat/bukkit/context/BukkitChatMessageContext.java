package com.seventh_root.elysium.chat.bukkit.context;

import com.seventh_root.elysium.api.chat.ChatMessageContext;
import com.seventh_root.elysium.api.chat.ElysiumChatChannel;
import com.seventh_root.elysium.api.player.ElysiumPlayer;

public class BukkitChatMessageContext implements ChatMessageContext {

    private final ElysiumChatChannel chatChannel;
    private final ElysiumPlayer sender;
    private final ElysiumPlayer receiver;

    public BukkitChatMessageContext(ElysiumChatChannel chatChannel, ElysiumPlayer sender, ElysiumPlayer receiver) {
        this.chatChannel = chatChannel;
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public ElysiumChatChannel getChatChannel() {
        return chatChannel;
    }

    @Override
    public ElysiumPlayer getSender() {
        return sender;
    }

    @Override
    public ElysiumPlayer getReceiver() {
        return receiver;
    }

}
