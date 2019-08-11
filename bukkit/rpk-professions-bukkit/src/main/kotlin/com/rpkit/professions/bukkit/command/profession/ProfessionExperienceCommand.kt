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
import com.rpkit.professions.bukkit.command.profession.experience.ProfessionExperienceAddCommand
import com.rpkit.professions.bukkit.command.profession.experience.ProfessionExperienceRemoveCommand
import com.rpkit.professions.bukkit.command.profession.experience.ProfessionExperienceSetCommand
import com.rpkit.professions.bukkit.command.profession.experience.ProfessionExperienceViewCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ProfessionExperienceCommand(val plugin: RPKProfessionsBukkit): CommandExecutor {

    private val professionExperienceAddCommand = ProfessionExperienceAddCommand(plugin)
    private val professionExperienceRemoveCommand = ProfessionExperienceRemoveCommand(plugin)
    private val professionExperienceSetCommand = ProfessionExperienceSetCommand(plugin)
    private val professionExperienceViewCommand = ProfessionExperienceViewCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["profession-experience-usage"])
            return true
        }
        val newArgs = args.drop(1).toTypedArray()
        return when (args[0]) {
            "add" -> professionExperienceAddCommand.onCommand(sender, command, label, newArgs)
            "remove" -> professionExperienceRemoveCommand.onCommand(sender, command, label, newArgs)
            "set" -> professionExperienceSetCommand.onCommand(sender, command, label, newArgs)
            "view", "show" -> professionExperienceViewCommand.onCommand(sender, command, label, newArgs)
            else -> {
                sender.sendMessage(plugin.messages["profession-experience-usage"])
                return true
            }
        }
    }


}
