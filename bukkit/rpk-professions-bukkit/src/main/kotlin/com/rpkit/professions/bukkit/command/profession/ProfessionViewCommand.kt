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

package com.rpkit.professions.bukkit.command.profession

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ProfessionViewCommand(val plugin: RPKProfessionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.professions.command.profession.view")) {
            sender.sendMessage(plugin.messages["no-permission-profession-view"])
            return true
        }
        val target = if (sender.hasPermission("rpkit.professions.command.profession.view.other")) {
            when {
                args.size > 1 -> {
                    val player = plugin.server.getPlayer(args[0])
                    if (player == null) {
                        sender.sendMessage(plugin.messages["profession-view-invalid-player-not-online"])
                        return true
                    } else {
                        player
                    }
                }
                sender is Player -> sender
                else -> {
                    sender.sendMessage(plugin.messages["profession-view-invalid-player-please-specify-from-console"])
                    return true
                }
            }
        } else {
            if (sender is Player) {
                sender
            } else {
                sender.sendMessage(plugin.messages["profession-view-invalid-player-please-specify-from-console"])
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
            if (target == sender) {
                sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile-other", mapOf(
                        "player" to target.name
                )])
            }
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            if (target == sender) {
                sender.sendMessage(plugin.messages["no-character-self"])
            } else {
                sender.sendMessage(plugin.messages["no-character-other", mapOf(
                        "player" to target.name
                )])
            }
            return true
        }
        val professionService = Services[RPKProfessionService::class.java]
        if (professionService == null) {
            sender.sendMessage(plugin.messages["no-profession-service"])
            return true
        }
        sender.sendMessage(plugin.messages["profession-view-valid-title"])
        professionService.getProfessions(character).forEach { profession ->
            val level = professionService.getProfessionLevel(character, profession)
            val experienceSinceLastLevel =
                    if (level > 1) {
                        professionService.getProfessionExperience(character, profession) - profession.getExperienceNeededForLevel(level)
                    } else {
                        professionService.getProfessionExperience(character, profession)
                    }
            val experienceNeededForNextLevel =
                    profession.getExperienceNeededForLevel(level + 1) - profession.getExperienceNeededForLevel(level)
            sender.sendMessage(plugin.messages["profession-view-valid-item", mapOf(
                    "profession" to profession.name.value,
                    "level" to level.toString(),
                    "total_experience" to professionService.getProfessionExperience(character, profession).toString(),
                    "total_next_level_experience" to profession.getExperienceNeededForLevel(level + 1).toString(),
                    "experience" to experienceSinceLastLevel.toString(),
                    "next_level_experience" to experienceNeededForNextLevel.toString()
            )])
        }
        return true
    }

}
