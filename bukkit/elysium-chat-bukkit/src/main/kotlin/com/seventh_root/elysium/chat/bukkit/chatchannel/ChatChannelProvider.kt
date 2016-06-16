package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.core.service.ServiceProvider

interface ChatChannelProvider<T : ElysiumChatChannel> : ServiceProvider {

    val chatChannels: Collection<T>
    fun getChatChannel(id: Int): T?
    fun getChatChannel(name: String): T?
    fun addChatChannel(chatChannel: T)
    fun removeChatChannel(chatChannel: T)
    fun updateChatChannel(chatChannel: T)
    fun getPlayerChannel(player: ElysiumPlayer): T?
    fun setPlayerChannel(player: ElysiumPlayer, channel: T)
    fun getChatChannelFromIRCChannel(ircChannel: String): T?

}
