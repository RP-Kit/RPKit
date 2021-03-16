/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKProfessionName
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ProfessionExperienceAddCommand(val plugin: RPKProfessionsBukkit) : CommandExecutor {

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
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(target)
        if (minecraftProfile == null) {
            if (sender == target) {
                sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile-other"])
            }
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            if (sender == target) {
                sender.sendMessage(plugin.messages["no-character-self"])
            } else {
                sender.sendMessage(plugin.messages["no-character-other"])
            }
            return true
        }
        val professionService = Services[RPKProfessionService::class.java]
        if (professionService == null) {
            sender.sendMessage(plugin.messages["no-profession-service"])
            return true
        }
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
        val profession = professionService.getProfession(RPKProfessionName(args[argsOffset]))
        if (profession == null) {
            sender.sendMessage(plugin.messages["profession-experience-add-invalid-profession"])
            return true
        }
        professionService.setProfessionExperience(character, profession, professionService.getProfessionExperience(character, profession) + exp)
        sender.sendMessage(plugin.messages["profession-experience-add-valid", mapOf(
                "player" to minecraftProfile.name,
                "character" to if (!character.isNameHidden) character.name else "[HIDDEN]",
                "profession" to profession.name.value,
                "total_experience" to professionService.getProfessionExperience(character, profession).toString(),
                "received_experience" to exp.toString()
        )])
        return true
    }

}
