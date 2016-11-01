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

package com.seventh_root.elysium.characters.bukkit.command.character.create

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterImpl
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CharacterNewCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.characters.command.character.new")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = ElysiumCharacterImpl(plugin, player = player)
                characterProvider.addCharacter(character)
                characterProvider.setActiveCharacter(player, character)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-new-valid")))
                character.showCharacterCard(player)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-new"))))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

}
