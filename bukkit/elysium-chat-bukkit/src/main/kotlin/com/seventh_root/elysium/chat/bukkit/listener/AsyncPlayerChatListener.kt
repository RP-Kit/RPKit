package com.seventh_root.elysium.chat.bukkit.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.context.BukkitChatMessageContext
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class AsyncPlayerChatListener(private val plugin: ElysiumChatBukkit) : Listener {

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        val bukkitPlayer = event.player
        val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
        val player = playerProvider.getPlayer(bukkitPlayer)
        var channel: BukkitChatChannel? = chatChannelProvider.getPlayerChannel(player)
        var message = event.message
        for (otherChannel in chatChannelProvider.chatChannels) {
            if (!otherChannel.matchPattern.isEmpty() && message.matches(otherChannel.matchPattern.toRegex())) {
                channel = otherChannel
                val pattern = Pattern.compile(otherChannel.matchPattern)
                val matcher = pattern.matcher(message)
                if (matcher.matches()) {
                    if (matcher.groupCount() > 0) {
                        message = matcher.group(1)
                    }
                }
                if (!channel.listeners.contains(player)) {
                    channel.addListener(player)
                    chatChannelProvider.updateChatChannel(channel)
                }
            }
        }
        if (channel != null) {
            for (listener in channel.listeners) {
                if (listener is BukkitPlayer) {
                    val bukkitOfflinePlayer = listener.bukkitPlayer
                    if (bukkitOfflinePlayer.isOnline) {
                        if (channel.radius <= 0
                                || (bukkitOfflinePlayer.player.world == bukkitPlayer.world
                                    && bukkitPlayer.location.distanceSquared(bukkitOfflinePlayer.player.location) <= channel.radius * channel.radius)
                        ) {
                            val processedMessage = channel.processMessage(message, BukkitChatMessageContext(channel, player, listener))
                            if (processedMessage != null) {
                                bukkitOfflinePlayer.player.sendMessage(processedMessage)
                            }
                        }
                    }
                }
            }
        } else {
            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channel")))
        }
    }

}
