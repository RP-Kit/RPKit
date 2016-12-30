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

package com.rpkit.stats.bukkit.command.stats

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.stats.bukkit.RPKStatsBukkit
import com.rpkit.stats.bukkit.stat.RPKStatProvider
import com.rpkit.stats.bukkit.stat.RPKStatVariableProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Stats command.
 * Shows all stat values for the player's active character.
 */
class StatsCommand(private val plugin: RPKStatsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.stats.command.stats")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    val statsProvider = plugin.core.serviceManager.getServiceProvider(RPKStatProvider::class)
                    val statVariableProvider = plugin.core.serviceManager.getServiceProvider(RPKStatVariableProvider::class)
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