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

package com.rpkit.statbuilds.bukkit.command.statbuild

import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StatBuildCommand(private val plugin: RPKStatBuildsBukkit) : CommandExecutor {

    private val statBuildAssignPointCommand = StatBuildAssignPointCommand(plugin)
    private val statBuildViewCommand = StatBuildViewCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["stat-build-usage"])
            return true
        }
        val newArgs = args.drop(1).toTypedArray()
        return when (args[0]) {
            "assign", "assignpoint", "asp" -> statBuildAssignPointCommand.onCommand(sender, command, label, newArgs)
            "view", "show" -> statBuildViewCommand.onCommand(sender, command, label, newArgs)
            else -> {
                sender.sendMessage(plugin.messages["stat-build-usage"])
                true
            }
        }
    }
}