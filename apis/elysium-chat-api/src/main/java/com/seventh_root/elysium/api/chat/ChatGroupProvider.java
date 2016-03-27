package com.seventh_root.elysium.api.chat;

import com.seventh_root.elysium.core.service.ServiceProvider;

public interface ChatGroupProvider<T extends ElysiumChatGroup> extends ServiceProvider {

    T getChatGroup(int id);

    T getChatGroup(String name);

    void addChatGroup(T chatGroup);

    void removeChatGroup(T chatGroup);

    void updateChatGroup(T chatGroup);

}
