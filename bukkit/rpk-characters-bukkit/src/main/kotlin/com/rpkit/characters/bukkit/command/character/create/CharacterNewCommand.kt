/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
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
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.new")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterNew)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return true
        }
        val newCharacterCooldownService = Services[RPKNewCharacterCooldownService::class.java]
        if (newCharacterCooldownService == null) {
            sender.sendMessage(plugin.messages.noNewCharacterCooldownService)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return true
        }
        newCharacterCooldownService.getNewCharacterCooldown(profile).thenAccept { newCharacterCooldown ->
            if (!sender.hasPermission("rpkit.characters.command.character.new.nocooldown")
                && !newCharacterCooldown.isNegative && !newCharacterCooldown.isZero) {
                sender.sendMessage(plugin.messages.characterNewInvalidCooldown)
                return@thenAccept
            }
            plugin.server.scheduler.runTask(plugin, Runnable {
                characterService.createCharacter(profile = profile).thenAccept { character ->
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        characterService.setActiveCharacter(minecraftProfile, character).thenRun {
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                newCharacterCooldownService.setNewCharacterCooldown(
                                    profile,
                                    Duration.of(plugin.config.getLong("characters.new-character-cooldown"), MILLIS)
                                ).thenRun {
                                    sender.sendMessage(plugin.messages.characterNewValid)
                                    character.showCharacterCard(minecraftProfile)
                                }
                            })
                        }
                    })
                }
            })
        }
        return true
    }

}
