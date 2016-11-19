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

package com.seventh_root.elysium.characters.bukkit.command.character.list

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Character list command.
 * Lists player's characters.
 */
class CharacterListCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.characters.command.character.list")) {
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val player = playerProvider.getPlayer(sender)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-list-title")))
                for (character in characterProvider.getCharacters(player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-list-item"))
                            .replace("\$character", character.name))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-list")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

}
