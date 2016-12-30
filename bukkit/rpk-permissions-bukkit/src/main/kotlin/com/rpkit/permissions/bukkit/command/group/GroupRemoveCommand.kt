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

package com.rpkit.permissions.bukkit.command.group

import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Group remove command.
 * Removes a group.
 */
class GroupRemoveCommand(private val plugin: RPKPermissionsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.permissions.command.group.remove")) {
            if (args.size > 1) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
                val bukkitPlayer = plugin.server.getPlayer(args[0])
                if (bukkitPlayer != null) {
                    val player = playerProvider.getPlayer(bukkitPlayer)
                    val group = groupProvider.getGroup(args[1])
                    if (group != null) {
                        groupProvider.removeGroup(player, group)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.group-remove-valid"))
                                .replace("\$group", group.name)
                                .replace("\$player", player.name))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.group-remove-invalid-group")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.group-remove-invalid-player")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.group-remove-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-")))
        }
        return true
    }

}