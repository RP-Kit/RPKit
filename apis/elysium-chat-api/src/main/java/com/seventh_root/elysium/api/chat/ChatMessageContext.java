package com.seventh_root.elysium.api.chat;

import com.seventh_root.elysium.api.player.ElysiumPlayer;

public interface ChatMessageContext {

    ElysiumChatChannel getChatChannel();

    ElysiumPlayer getSender();

    ElysiumPlayer getReceiver();

}
