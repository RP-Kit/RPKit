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

package com.seventh_root.elysium.stats.bukkit.command.stats

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import com.seventh_root.elysium.stats.bukkit.ElysiumStatsBukkit
import com.seventh_root.elysium.stats.bukkit.stat.ElysiumStatProvider
import com.seventh_root.elysium.stats.bukkit.stat.ElysiumStatVariableProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Stats command.
 * Shows all stat values for the player's active character.
 */
class StatsCommand(private val plugin: ElysiumStatsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("elysium.stats.command.stats")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    val statsProvider = plugin.core.serviceManager.getServiceProvider(ElysiumStatProvider::class)
                    val statVariableProvider = plugin.core.serviceManager.getServiceProvider(ElysiumStatVariableProvider::class)
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.stats-list-title")))
                    statsProvider.stats.forEach { stat ->
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.stats-list-item"))
                            .replace("\$stat", stat.name)
                            .replace("\$value",
                                    stat.get(character, statVariableProvider.statVariables).toString()
                            )
                        )
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-stats")))
        }
        return true
    }

}