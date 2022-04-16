/*
 * Copyright 2020 Ren Binden
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

import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Group command.
 * Parent command for all group management commands.
 */
class GroupCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    private val groupAddCommand = GroupAddCommand(plugin)
    private val groupRemoveCommand = GroupRemoveCommand(plugin)
    private val groupListCommand = GroupListCommand(plugin)
    private val groupViewCommand = GroupViewCommand(plugin)
    private val groupPrepareSwitchPriorityCommand = GroupPrepareSwitchPriorityCommand(plugin)
    private val groupSwitchPriorityCommand = GroupSwitchPriorityCommand(plugin).toBukkit()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["group-usage"])
            return true
        }
        when (args[0].lowercase()) {
            "add" -> return groupAddCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "remove" -> return groupRemoveCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "list" -> return groupListCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "view" -> return groupViewCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "prepareswitchpriority" -> return groupPrepareSwitchPriorityCommand.onCommand(sender, command, label, args.drop(1).toTypedArray()   )
            "switchpriority" -> return groupSwitchPriorityCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> sender.sendMessage(plugin.messages.groupUsage)
        }
        return true
    }

}
