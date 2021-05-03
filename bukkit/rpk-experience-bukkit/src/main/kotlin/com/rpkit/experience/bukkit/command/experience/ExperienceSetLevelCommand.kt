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

package com.rpkit.experience.bukkit.command.experience

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ExperienceSetLevelCommand(private val plugin: RPKExperienceBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.experience.command.experience.setlevel")) {
            sender.sendMessage(plugin.messages["no-permission-experience-setlevel"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["experience-setlevel-usage"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val experienceService = Services[RPKExperienceService::class.java]
        if (experienceService == null) {
            sender.sendMessage(plugin.messages["no-experience-service"])
            return true
        }
        val bukkitPlayer = plugin.server.getPlayer(args[0])
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages["experience-setlevel-player-invalid-player"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character-other"])
            return true
        }
        try {
            val level = args[1].toInt()
            experienceService.setExperience(character, experienceService.getExperienceNeededForLevel(level))
            sender.sendMessage(plugin.messages["experience-setlevel-valid"])
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["experience-setlevel-level-invalid-number"])
        }
        return true
    }
}