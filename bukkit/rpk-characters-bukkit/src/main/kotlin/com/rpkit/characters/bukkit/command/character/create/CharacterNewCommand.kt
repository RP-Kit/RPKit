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

package com.rpkit.characters.bukkit.command.character.create

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterImpl
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Character new command.
 * Creates a new character, then allows the player to modify fields from the defaults (specified in the config).
 */
class CharacterNewCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.characters.command.character.new")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val newCharacterCooldownProvider = plugin.core.serviceManager.getServiceProvider(RPKNewCharacterCooldownProvider::class)
                val player = playerProvider.getPlayer(sender)
                if (sender.hasPermission("rpkit.characters.command.character.new.nocooldown") || newCharacterCooldownProvider.getNewCharacterCooldown(player) <= 0) {
                    val character = RPKCharacterImpl(plugin, player = player)
                    characterProvider.addCharacter(character)
                    characterProvider.setActiveCharacter(player, character)
                    newCharacterCooldownProvider.setNewCharacterCooldown(player, plugin.config.getLong("characters.new-character-cooldown"))
                    sender.sendMessage(plugin.core.messages["character-new-valid"])
                    character.showCharacterCard(player)
                } else {
                    sender.sendMessage(plugin.core.messages["character-new-invalid-cooldown"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["no-permission-character-new"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["not-from-console"])
        }
        return true
    }

}
