package com.seventh_root.elysium.api.chat;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.service.ServiceProvider;

import java.util.Collection;

public interface ChatChannelProvider<T extends ElysiumChatChannel> extends ServiceProvider {

    Collection<? extends T> getChatChannels();

    T getChatChannel(int id);

    T getChatChannel(String name);

    void addChatChannel(T chatChannel);

    void removeChatChannel(T chatChannel);

    void updateChatChannel(T chatChannel);

    T getPlayerChannel(ElysiumPlayer player);

    void setPlayerChannel(ElysiumPlayer player, T channel);

    T getChatChannelFromIRCChannel(String ircChannel);

}
