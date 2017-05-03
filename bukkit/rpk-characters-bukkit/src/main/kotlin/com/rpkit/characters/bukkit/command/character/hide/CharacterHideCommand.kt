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

package com.rpkit.characters.bukkit.command.character.hide

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Character hide command.
 * Parent command for commands to hide different character card fields.
 */
class CharacterHideCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    private val characterHidePlayerCommand = CharacterHidePlayerCommand(plugin)
    private val characterHideProfileCommand = CharacterHideProfileCommand(plugin)
    private val characterHideNameCommand = CharacterHideNameCommand(plugin)
    private val characterHideGenderCommand = CharacterHideGenderCommand(plugin)
    private val characterHideAgeCommand = CharacterHideAgeCommand(plugin)
    private val characterHideRaceCommand = CharacterHideRaceCommand(plugin)
    private val characterHideDescriptionCommand = CharacterHideDescriptionCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("player", ignoreCase = true)) {
                return characterHidePlayerCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("profile", ignoreCase = true)) {
                return characterHideProfileCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("name", ignoreCase = true)) {
                return characterHideNameCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("gender", ignoreCase = true)) {
                return characterHideGenderCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("age", ignoreCase = true)) {
                return characterHideAgeCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("race", ignoreCase = true)) {
                return characterHideRaceCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("description", ignoreCase = true) || args[0].equals("desc", ignoreCase = true)) {
                return characterHideDescriptionCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["character-hide-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["character-hide-usage"])
        }
        return true
    }

}
