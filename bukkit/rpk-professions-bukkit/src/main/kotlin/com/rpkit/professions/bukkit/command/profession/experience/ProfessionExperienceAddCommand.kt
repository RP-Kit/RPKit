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

package com.rpkit.professions.bukkit.command.profession.experience

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKProfessionProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ProfessionExperienceAddCommand(val plugin: RPKProfessionsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.professions.command.profession.experience.add")) {
            sender.sendMessage(plugin.messages["no-permission-profession-experience-add"])
            return true
        }
        var argsOffset = 0
        val target = if (sender.hasPermission("rpkit.professions.command.profession.experience.add.other")) {
            when {
                args.size > 2 -> {
                    val player = plugin.server.getPlayer(args[0])
                    if (player == null) {
                        sender.sendMessage(plugin.messages["profession-experience-add-invalid-player-not-online"])
                        return true
                    } else {
                        argsOffset = 1
                        player
                    }
                }
                sender is Player -> sender
                else -> {
                    sender.sendMessage(plugin.messages["profession-experience-add-invalid-player-please-specify-from-console"])
                    return true
                }
            }
        } else {
            if (sender is Player) {
                sender
            } else {
                sender.sendMessage(plugin.messages["profession-experience-add-invalid-player-please-specify-from-console"])
                return true
            }
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(target)
        if (minecraftProfile == null) {
            if (sender == target) {
                sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile-other"])
            }
            return true
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        if (character == null) {
            if (sender == target) {
                sender.sendMessage(plugin.messages["no-character-self"])
            } else {
                sender.sendMessage(plugin.messages["no-character-other"])
            }
            return true
        }
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["profession-experience-add-usage"])
            return true
        }
        val exp = try {
            args[argsOffset + 1].toInt()
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["profession-experience-add-invalid-exp-not-a-number"])
            return true
        }
        val profession = professionProvider.getProfession(args[argsOffset])
        if (profession == null) {
            sender.sendMessage(plugin.messages["profession-experience-add-invalid-profession"])
            return true
        }
        professionProvider.setProfessionExperience(character, profession, professionProvider.getProfessionExperience(character, profession) + exp)
        sender.sendMessage(plugin.messages["profession-experience-add-valid", mapOf(
                "player" to minecraftProfile.minecraftUsername,
                "character" to if (!character.isNameHidden) character.name else "[HIDDEN]",
                "profession" to profession.name,
                "total-experience" to professionProvider.getProfessionExperience(character, profession).toString(),
                "received-experience" to exp.toString()
        )])
        return true
    }

}
