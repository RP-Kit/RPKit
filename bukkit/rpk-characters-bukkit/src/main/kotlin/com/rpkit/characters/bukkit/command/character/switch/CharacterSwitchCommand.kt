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

package com.rpkit.characters.bukkit.command.character.switch

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.levenshtein
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Character switch command.
 * Switches active character.
 */
class CharacterSwitchCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.switch")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSwitch)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.characterSwitchUsage)
            return true
        }
        val characterNameBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            characterNameBuilder.append(args[i]).append(" ")
        }
        characterNameBuilder.append(args[args.size - 1])
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfile)
            return true
        }
        characterService.getCharacters(profile).thenAccept { characters ->
            val characterName = characterNameBuilder.toString()
            val character = characters.minByOrNull { character -> character.name.levenshtein(characterName) }
            if (character == null) {
                sender.sendMessage(plugin.messages.characterSwitchInvalidCharacter)
                return@thenAccept
            }
            if (character.minecraftProfile != null) {
                sender.sendMessage(plugin.messages.characterSwitchInvalidCharacterOtherAccount)
                return@thenAccept
            }
            plugin.server.scheduler.runTask(plugin, Runnable {
                characterService.setActiveCharacter(minecraftProfile, character).thenRun {
                    sender.sendMessage(plugin.messages.characterSwitchValid)
                }
            })
        }
        return true
    }

}
