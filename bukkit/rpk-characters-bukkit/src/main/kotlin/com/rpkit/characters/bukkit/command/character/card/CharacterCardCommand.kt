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

package com.rpkit.characters.bukkit.command.character.card

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Character card command.
 * Displays the character card of the active character of either the player running the command or the specified player.
 */
class CharacterCardCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.characters.command.character.card.self")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                var player = playerProvider.getPlayer(sender)
                if (sender.hasPermission("rpkit.characters.command.character.card.other")) {
                    if (args.size > 0) {
                        val bukkitPlayer = plugin.server.getPlayer(args[0])
                        if (bukkitPlayer != null) {
                            player = playerProvider.getPlayer(bukkitPlayer)
                        }
                    }
                }
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    val senderPlayer = playerProvider.getPlayer(sender)
                    character.showCharacterCard(senderPlayer)
                } else {
                    sender.sendMessage(plugin.messages["no-character"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-character-card-self"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

}
