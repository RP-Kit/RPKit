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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKProfessionProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ProfessionViewCommand(val plugin: RPKProfessionsBukkit): CommandExecutor {

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
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        sender.sendMessage(plugin.messages["profession-view-valid-title"])
        professionProvider.getProfessions(character).forEach { profession ->
            val level = professionProvider.getProfessionLevel(character, profession)
            val experienceSinceLastLevel =
                    if (level > 1) {
                        professionProvider.getProfessionExperience(character, profession) - profession.getExperienceNeededForLevel(level)
                    } else {
                        professionProvider.getProfessionExperience(character, profession)
                    }
            val experienceNeededForNextLevel =
                    profession.getExperienceNeededForLevel(level + 1) - profession.getExperienceNeededForLevel(level)
            sender.sendMessage(plugin.messages["profession-view-valid-item", mapOf(
                    "profession" to profession.name,
                    "level" to level.toString(),
                    "total-experience" to professionProvider.getProfessionExperience(character, profession).toString(),
                    "total-next-level-experience" to profession.getExperienceNeededForLevel(level + 1).toString(),
                    "experience" to experienceSinceLastLevel.toString(),
                    "next-level-experience" to experienceNeededForNextLevel.toString()
            )])
        }
        return true
    }

}
