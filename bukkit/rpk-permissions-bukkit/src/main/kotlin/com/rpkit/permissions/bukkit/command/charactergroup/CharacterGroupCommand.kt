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

package com.rpkit.permissions.bukkit.command.charactergroup

import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Character group command.
 * Parent command for all character group management commands.
 */
class CharacterGroupCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    private val characterGroupAddCommand = CharacterGroupAddCommand(plugin)
    private val characterGroupRemoveCommand = CharacterGroupRemoveCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            when {
                args[0].equals("add", ignoreCase = true) -> return characterGroupAddCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
                args[0].equals("remove", ignoreCase = true) -> return characterGroupRemoveCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
                else -> sender.sendMessage(plugin.messages["character-group-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["character-group-usage"])
        }
        return true
    }

}