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

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import com.seventh_root.elysium.chat.bukkit.snooper.ElysiumSnooperProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent


class PlayerCommandPreprocessListener(private val plugin: ElysiumChatBukkit): Listener {

    @EventHandler
    fun onPlayerCommandPreProcess(event: PlayerCommandPreprocessEvent) {
        val snooperProvider = plugin.core.serviceManager.getServiceProvider(ElysiumSnooperProvider::class)
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val sender = playerProvider.getPlayer(event.player)
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        val snoopers = snooperProvider.snoopers
        var commandMessage = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.command-snoop"))
        if (commandMessage.contains("\$command")) {
            commandMessage = commandMessage.replace("\$command", event.message)
        }
        if (commandMessage.contains("\$sender-prefix")) {
            commandMessage = commandMessage.replace("\$sender-prefix", prefixProvider.getPrefix(sender))
        }
        if (commandMessage.contains("\$sender-player")) {
            commandMessage = commandMessage.replace("\$sender-player", sender.name)
        }
        if (commandMessage.contains("\$sender-character")) {
            if (senderCharacter != null) {
                commandMessage = commandMessage.replace("\$sender-character", senderCharacter.name)
            }
        }
        snoopers.forEach { snooper -> snooper.bukkitPlayer?.player?.sendMessage(commandMessage) }
    }

}