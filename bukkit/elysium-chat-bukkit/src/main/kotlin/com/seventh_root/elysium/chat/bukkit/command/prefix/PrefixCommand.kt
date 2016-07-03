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
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class PrefixCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {

    private val prefixAddCommand = PrefixAddCommand(plugin)
    private val prefixRemoveCommand = PrefixRemoveCommand(plugin)
    private val prefixListCommand = PrefixListCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size > 0) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("add", ignoreCase = true) || args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return prefixAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("remove", ignoreCase = true) || args[0].equals("delete", ignoreCase = true)) {
                return prefixRemoveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return prefixListCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-usage")))
        }
        return true
    }
}