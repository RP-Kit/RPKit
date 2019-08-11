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

class ProfessionExperienceViewCommand(val plugin: RPKProfessionsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.professions.command.profession.experience.view")) {
            sender.sendMessage(plugin.messages["no-permission-profession-experience-view"])
            return true
        }
        var argsOffset = 0
        val target = if (sender.hasPermission("rpkit.professions.command.profession.experience.view.other")) {
            when {
                args.size > 2 -> {
                    val player = plugin.server.getPlayer(args[0])
                    if (player == null) {
                        sender.sendMessage(plugin.messages["profession-experience-view-invalid-player-not-online"])
                        return true
                    } else {
                        argsOffset = 1
                        player
                    }
                }
                sender is Player -> sender
                else -> {
                    sender.sendMessage(plugin.messages["profession-experience-view-invalid-player-please-specify-from-console"])
                    return true
                }
            }
        } else {
            if (sender is Player) {
                sender
            } else {
                sender.sendMessage(plugin.messages["profession-experience-view-invalid-player-please-specify-from-console"])
                return true
            }
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(target)
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
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getActiveCharacter(minecraftProfile)
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
        if (args.size <= argsOffset) {
            sender.sendMessage(plugin.messages["profession-experience-view-usage"])
            return true
        }
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        val profession = professionProvider.getProfession(args[argsOffset])
        if (profession == null) {
            sender.sendMessage(plugin.messages["profession-experience-view-invalid-profession"])
            return true
        }
        val level = professionProvider.getProfessionLevel(character, profession)
        val experienceSinceLastLevel =
                if (level > 1) {
                    professionProvider.getProfessionExperience(character, profession) - profession.getExperienceNeededForLevel(level)
                } else {
                    professionProvider.getProfessionExperience(character, profession)
                }
        val experienceNeededForNextLevel =
                profession.getExperienceNeededForLevel(level + 1) - profession.getExperienceNeededForLevel(level)
        sender.sendMessage(plugin.messages["profession-experience-view-valid", mapOf(
                "profession" to profession.name,
                "level" to level.toString(),
                "total-experience" to professionProvider.getProfessionExperience(character, profession).toString(),
                "total-next-level-experience" to profession.getExperienceNeededForLevel(level + 1).toString(),
                "experience" to experienceSinceLastLevel.toString(),
                "next-level-experience" to experienceNeededForNextLevel.toString()
        )])
        return true
    }

}
