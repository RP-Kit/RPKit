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

package com.rpkit.characters.bukkit.command.race

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Race command.
 * Parent for all race management commands.
 */
class RaceCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {
    private val raceAddCommand: RaceAddCommand
    private val raceRemoveCommand: RaceRemoveCommand
    private val raceListCommand: RaceListCommand

    init {
        this.raceAddCommand = RaceAddCommand(plugin)
        this.raceRemoveCommand = RaceRemoveCommand(plugin)
        this.raceListCommand = RaceListCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("add", ignoreCase = true) || args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return raceAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("remove", ignoreCase = true) || args[0].equals("delete", ignoreCase = true)) {
                return raceRemoveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return raceListCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.core.messages["race-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["race-usage"])
        }
        return true
    }

}
