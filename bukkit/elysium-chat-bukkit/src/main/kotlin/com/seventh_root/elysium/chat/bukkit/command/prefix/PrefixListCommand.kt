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

package com.seventh_root.elysium.chat.bukkit.command.prefix

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class PrefixListCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("elysium.chat.command.prefix.list")) {
            val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-list-title")))
            for (prefix in prefixProvider.prefixes) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-list-item"))
                        .replace("\$prefix", prefix.name))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-prefix-list")))
        }
        return true
    }
}