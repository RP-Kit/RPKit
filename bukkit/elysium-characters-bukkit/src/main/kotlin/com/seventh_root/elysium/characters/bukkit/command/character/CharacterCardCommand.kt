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

package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.characters.bukkit.character.field.ElysiumCharacterCardFieldProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CharacterCardCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.characters.command.character.card.self")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                var player = playerProvider.getPlayer(sender)
                if (sender.hasPermission("elysiumcharacters.command.character.card.other")) {
                    if (args.size > 0) {
                        val bukkitPlayer = plugin.server.getPlayer(args[0])
                        if (bukkitPlayer != null) {
                            player = playerProvider.getPlayer(bukkitPlayer)
                        }
                    }
                }
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    for (line in plugin.config.getStringList("messages.character-card")) {
                        var filteredLine = ChatColor.translateAlternateColorCodes('&', line)
                        val characterCardFieldProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterCardFieldProvider::class.java)
                        characterCardFieldProvider.characterCardFields.forEach { field -> filteredLine = filteredLine.replace("\$${field.name}", field.get(character)) }
                        sender.sendMessage(filteredLine)
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-card-self")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

}
