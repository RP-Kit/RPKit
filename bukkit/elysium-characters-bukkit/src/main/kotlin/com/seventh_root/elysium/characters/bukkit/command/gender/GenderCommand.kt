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

package com.seventh_root.elysium.characters.bukkit.command.gender

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GenderCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {
    private val genderAddCommand: GenderAddCommand
    private val genderRemoveCommand: GenderRemoveCommand
    private val genderListCommand: GenderListCommand

    init {
        this.genderAddCommand = GenderAddCommand(plugin)
        this.genderRemoveCommand = GenderRemoveCommand(plugin)
        this.genderListCommand = GenderListCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            var newArgsList: MutableList<String> = arrayListOf()
            for (i: Int in 1..(args.size - 1)) {
                newArgsList.add(args[i])
            }
            var newArgs = newArgsList.toTypedArray()
            if (args[0].equals("add", ignoreCase = true) || args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return genderAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("remove", ignoreCase = true) || args[0].equals("delete", ignoreCase = true)) {
                return genderRemoveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return genderListCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-usage")))
        }
        return true
    }

}
