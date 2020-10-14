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

package com.rpkit.characters.bukkit.command.character.create

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterImpl
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

/**
 * Character new command.
 * Creates a new character, then allows the player to modify fields from the defaults (specified in the config).
 */
class CharacterNewCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.new")) {
            sender.sendMessage(plugin.messages["no-permission-character-new"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val newCharacterCooldownService = Services[RPKNewCharacterCooldownService::class]
        if (newCharacterCooldownService == null) {
            sender.sendMessage(plugin.messages["no-new-character-cooldown-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val newCharacterCooldown = newCharacterCooldownService.getNewCharacterCooldown(profile)
        if (!sender.hasPermission("rpkit.characters.command.character.new.nocooldown")
                && !newCharacterCooldown.isNegative && !newCharacterCooldown.isZero) {
            sender.sendMessage(plugin.messages["character-new-invalid-cooldown"])
            return true
        }
        val character = RPKCharacterImpl(plugin, profile = profile)
        characterService.addCharacter(character)
        characterService.setActiveCharacter(minecraftProfile, character)
        newCharacterCooldownService.setNewCharacterCooldown(profile, Duration.of(plugin.config.getLong("characters.new-character-cooldown"), MILLIS))
        sender.sendMessage(plugin.messages["character-new-valid"])
        character.showCharacterCard(minecraftProfile)
        return true
    }

}
