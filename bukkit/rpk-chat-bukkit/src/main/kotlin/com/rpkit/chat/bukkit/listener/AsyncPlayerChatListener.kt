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

package com.rpkit.chat.bukkit.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Player chat listener.
 * Cancels normal message processing and passes the message to the appropriate chat channel.
 */
class AsyncPlayerChatListener(private val plugin: RPKChatBukkit): Listener {

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val player = playerProvider.getPlayer(event.player)
        val chatChannel = chatChannelProvider.getPlayerChannel(player)
        if (chatChannel != null) {
            chatChannel.sendMessage(player, event.message)
        } else {
            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channel")))
        }
    }

}
