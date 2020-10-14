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

package com.rpkit.moderation.bukkit.command.warn

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class WarningCommand(private val plugin: RPKModerationBukkit) : CommandExecutor {

    private val warningCreateCommand = WarningCreateCommand(plugin)
    private val warningRemoveCommand = WarningRemoveCommand(plugin)
    private val warningListCommand = WarningListCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.moderation.command.warning")) {
            sender.sendMessage(plugin.messages["no-permission-warning"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["warning-usage"])
            return true
        }
        return when (args[0]) {
            "create", "add" -> warningCreateCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "delete", "remove" -> warningRemoveCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "list" -> warningListCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage(plugin.messages["warning-usage"])
                true
            }
        }
    }

}