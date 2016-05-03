package com.seventh_root.elysium.chat.bukkit.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: ElysiumChatBukkit) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!event.player.hasPlayedBefore()) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
            val player = playerProvider.getPlayer(event.player)
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
            chatChannelProvider.chatChannels.filter { it -> it.isJoinedByDefault }.forEach({ chatChannel ->
                chatChannel.addListener(player)
                chatChannelProvider.updateChatChannel(chatChannel)
            })
        }
    }

}
