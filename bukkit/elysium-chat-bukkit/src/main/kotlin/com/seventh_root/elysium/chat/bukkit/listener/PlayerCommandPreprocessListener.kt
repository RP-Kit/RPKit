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
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/**
 * Player command preprocess listener.
 * Picks up commands before they are sent to normal chat processing, allowing them to be interpreted as chat channel
 * commands.
 * Hacky and circumvents the command system, but users are stuck in their ways.
 */
class PlayerCommandPreprocessListener(private val plugin: ElysiumChatBukkit): Listener {

    @EventHandler
    fun onPlayerCommandPreProcess(event: PlayerCommandPreprocessEvent) {
        val chatChannelName = event.message.split(Regex("\\s+"))[0].drop(1)
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
        val chatChannel = chatChannelProvider.getChatChannel(chatChannelName)
        if (chatChannel != null) {
            if (event.player.hasPermission("elysium.chat.command.chatchannel.${chatChannel.name}")) {
                event.isCancelled = true
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val player = playerProvider.getPlayer(event.player)
                chatChannel.sendMessage(player, event.message.split(Regex("\\s+")).drop(1).joinToString(" "))
            }
        }
    }

}