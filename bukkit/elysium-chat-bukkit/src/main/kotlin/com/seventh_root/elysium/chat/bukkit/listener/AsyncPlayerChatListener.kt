/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seventh_root.elysium.chat.bukkit.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContextImpl
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContextImpl
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class AsyncPlayerChatListener(private val plugin: ElysiumChatBukkit): Listener {

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        val bukkitPlayer = event.player
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
        val player = playerProvider.getPlayer(bukkitPlayer)
        var channel: ElysiumChatChannel? = chatChannelProvider.getPlayerChannel(player)
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
                val bukkitOfflinePlayer = listener.bukkitPlayer
                if (bukkitOfflinePlayer != null) {
                    if (bukkitOfflinePlayer.isOnline) {
                        if (channel.radius <= 0
                                || (bukkitOfflinePlayer.player.world == bukkitPlayer.world
                                && bukkitPlayer.location.distanceSquared(bukkitOfflinePlayer.player.location) <= channel.radius * channel.radius)
                        ) {
                            val processedMessage = channel.processMessage(message, ChatMessageContextImpl(channel, player, listener))
                            if (processedMessage != null) {
                                bukkitOfflinePlayer.player.sendMessage(processedMessage)
                            }
                        }
                    }
                }
            }
            if (channel.isIRCEnabled) {
                val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class)
                val processedMessage = channel.postProcess(message, ChatMessagePostProcessContextImpl(channel, player))
                ircProvider.ircBot.sendIRC().message(channel.ircChannel, ChatColor.stripColor(processedMessage))
            } else {
                channel.postProcess(message, ChatMessagePostProcessContextImpl(channel, player))
            }
        } else {
            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channel")))
        }
    }

}
