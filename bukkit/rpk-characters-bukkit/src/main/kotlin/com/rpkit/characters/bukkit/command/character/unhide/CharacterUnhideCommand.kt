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

package com.rpkit.characters.bukkit.command.character.unhide

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Character unhide command.
 * Parent command for commands to unhide different character card fields.
 */
class CharacterUnhideCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    private val characterUnhidePlayerCommand = CharacterUnhidePlayerCommand(plugin)
    private val characterUnhideNameCommand = CharacterUnhideNameCommand(plugin)
    private val characterUnhideGenderCommand = CharacterUnhideGenderCommand(plugin)
    private val characterUnhideAgeCommand = CharacterUnhideAgeCommand(plugin)
    private val characterUnhideRaceCommand = CharacterUnhideRaceCommand(plugin)
    private val characterUnhideDescriptionCommand = CharacterUnhideDescriptionCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("player", ignoreCase = true)) {
                return characterUnhidePlayerCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("name", ignoreCase = true)) {
                return characterUnhideNameCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("gender", ignoreCase = true)) {
                return characterUnhideGenderCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("age", ignoreCase = true)) {
                return characterUnhideAgeCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("race", ignoreCase = true)) {
                return characterUnhideRaceCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("description", ignoreCase = true) || args[0].equals("desc", ignoreCase = true)) {
                return characterUnhideDescriptionCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.core.messages["character-unhide-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["character-unhide-usage"])
        }
        return true
    }

}
