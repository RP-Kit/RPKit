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

package com.rpkit.characters.bukkit.command.character

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.command.character.card.CharacterCardCommand
import com.rpkit.characters.bukkit.command.character.create.CharacterNewCommand
import com.rpkit.characters.bukkit.command.character.delete.CharacterDeleteCommand
import com.rpkit.characters.bukkit.command.character.hide.CharacterHideCommand
import com.rpkit.characters.bukkit.command.character.list.CharacterListCommand
import com.rpkit.characters.bukkit.command.character.set.CharacterSetCommand
import com.rpkit.characters.bukkit.command.character.switch.CharacterSwitchCommand
import com.rpkit.characters.bukkit.command.character.unhide.CharacterUnhideCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Character command.
 * Parent command for all character management commands.
 */
class CharacterCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    private val characterSetCommand: CharacterSetCommand
    private val characterHideCommand: CharacterHideCommand
    private val characterUnhideCommand: CharacterUnhideCommand
    private val characterCardCommand: CharacterCardCommand
    private val characterSwitchCommand: CharacterSwitchCommand
    private val characterListCommand: CharacterListCommand
    private val characterNewCommand: CharacterNewCommand
    private val characterDeleteCommand: CharacterDeleteCommand

    init {
        characterSetCommand = CharacterSetCommand(plugin)
        characterHideCommand = CharacterHideCommand(plugin)
        characterUnhideCommand = CharacterUnhideCommand(plugin)
        characterCardCommand = CharacterCardCommand(plugin)
        characterSwitchCommand = CharacterSwitchCommand(plugin)
        characterListCommand = CharacterListCommand(plugin)
        characterNewCommand = CharacterNewCommand(plugin)
        characterDeleteCommand = CharacterDeleteCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("set", ignoreCase = true)) {
                return characterSetCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("hide", ignoreCase = true)) {
                return characterHideCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("unhide", ignoreCase = true)) {
                return characterUnhideCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("card", ignoreCase = true) || args[0].equals("show", ignoreCase = true) || args[0].equals("view", ignoreCase = true)) {
                return characterCardCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("switch", ignoreCase = true)) {
                return characterSwitchCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return characterListCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("new", ignoreCase = true) || args[0].equals("create", ignoreCase = true)) {
                return characterNewCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("delete", ignoreCase = true)) {
                return characterDeleteCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["character-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["character-usage"])
        }
        return true
    }

}
