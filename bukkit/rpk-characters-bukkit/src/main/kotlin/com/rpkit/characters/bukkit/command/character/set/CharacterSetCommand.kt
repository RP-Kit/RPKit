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

package com.rpkit.characters.bukkit.command.character.set

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Character set command.
 * Parent command for commands used to set character attributes.
 */
class CharacterSetCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    private val characterSetPlayerCommand: CharacterSetPlayerCommand
    private val characterSetNameCommand: CharacterSetNameCommand
    private val characterSetGenderCommand: CharacterSetGenderCommand
    private val characterSetAgeCommand: CharacterSetAgeCommand
    private val characterSetRaceCommand: CharacterSetRaceCommand
    private val characterSetDescriptionCommand: CharacterSetDescriptionCommand
    private val characterSetDeadCommand: CharacterSetDeadCommand

    init {
        characterSetPlayerCommand = CharacterSetPlayerCommand(plugin)
        characterSetNameCommand = CharacterSetNameCommand(plugin)
        characterSetGenderCommand = CharacterSetGenderCommand(plugin)
        characterSetAgeCommand = CharacterSetAgeCommand(plugin)
        characterSetRaceCommand = CharacterSetRaceCommand(plugin)
        characterSetDescriptionCommand = CharacterSetDescriptionCommand(plugin)
        characterSetDeadCommand = CharacterSetDeadCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("player", ignoreCase = true)) {
                return characterSetPlayerCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("name", ignoreCase = true)) {
                return characterSetNameCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("gender", ignoreCase = true)) {
                return characterSetGenderCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("age", ignoreCase = true)) {
                return characterSetAgeCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("race", ignoreCase = true)) {
                return characterSetRaceCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("description", ignoreCase = true) || args[0].equals("desc", ignoreCase = true)) {
                return characterSetDescriptionCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("dead", ignoreCase = true)) {
                return characterSetDeadCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-usage")))
        }
        return true
    }
}
