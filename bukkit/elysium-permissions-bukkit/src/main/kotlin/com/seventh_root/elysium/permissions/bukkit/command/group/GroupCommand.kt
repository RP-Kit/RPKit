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

package com.seventh_root.elysium.permissions.bukkit.command.group

import com.seventh_root.elysium.permissions.bukkit.ElysiumPermissionsBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Group command.
 * Parent command for all group management commands.
 */
class GroupCommand(private val plugin: ElysiumPermissionsBukkit): CommandExecutor {

    private val groupAddCommand = GroupAddCommand(plugin)
    private val groupRemoveCommand = GroupRemoveCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size > 0) {
            if (args[0].equals("add", ignoreCase = true)) {
                return groupAddCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            } else if (args[0].equals("remove", ignoreCase = true)) {
                return groupRemoveCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages/group-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages/group-usage")))
        }
        return true
    }

}