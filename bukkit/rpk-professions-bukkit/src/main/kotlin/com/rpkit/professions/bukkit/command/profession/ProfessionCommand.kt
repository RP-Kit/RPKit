/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.professions.bukkit.command.profession

import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ProfessionCommand(private val plugin: RPKProfessionsBukkit): CommandExecutor {

    private val professionListCommand = ProfessionListCommand(plugin)
    private val professionSetCommand = ProfessionSetCommand(plugin)
    private val professionUnsetCommand = ProfessionUnsetCommand(plugin)
    private val professionViewCommand = ProfessionViewCommand(plugin)
    private val professionExperienceCommand = ProfessionExperienceCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["profession-usage"])
            return true
        }
        val newArgs = args.drop(1).toTypedArray()
        return when (args[0]) {
            "list" -> professionListCommand.onCommand(sender, command, label, newArgs)
            "set" -> professionSetCommand.onCommand(sender, command, label, newArgs)
            "unset" -> professionUnsetCommand.onCommand(sender, command, label, newArgs)
            "view" -> professionViewCommand.onCommand(sender, command, label, newArgs)
            "experience", "exp", "xp" -> professionExperienceCommand.onCommand(sender, command, label, newArgs)
            else -> {
                sender.sendMessage(plugin.messages["profession-usage"])
                true
            }
        }
    }

}