package com.seventh_root.elysium.api.chat

import com.seventh_root.elysium.core.service.ServiceProvider

interface ChatGroupProvider<T : ElysiumChatGroup> : ServiceProvider {

    fun getChatGroup(id: Int): T
    fun getChatGroup(name: String): T
    fun addChatGroup(chatGroup: T)
    fun removeChatGroup(chatGroup: T)
    fun updateChatGroup(chatGroup: T)

}
