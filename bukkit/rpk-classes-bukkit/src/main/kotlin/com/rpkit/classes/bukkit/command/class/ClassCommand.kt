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

package com.rpkit.classes.bukkit.command.`class`

import com.rpkit.classes.bukkit.RPKClassesBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ClassCommand(private val plugin: RPKClassesBukkit) : CommandExecutor {

    private val classSetCommand = ClassSetCommand(plugin)
    private val classListCommand = ClassListCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            when {
                args[0].equals("set", ignoreCase = true) -> return classSetCommand.onCommand(sender, command, label, newArgs)
                args[0].equals("list", ignoreCase = true) -> return classListCommand.onCommand(sender, command, label, newArgs)
                else -> sender.sendMessage(plugin.messages["class-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["class-usage"])
        }
        return true
    }

}