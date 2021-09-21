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

package com.rpkit.languages.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguageService
import com.rpkit.languages.bukkit.language.RPKLanguageService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ListCharacterUnderstandingCommand(private val plugin: RPKLanguagesBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.languages.command.listunderstandings")) {
            sender.sendMessage(plugin.messages.noPermissionListCharacterLanguageUnderstanding)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.listCharacterLanguageUnderstandingUsage)
            return true
        }
        val player = Bukkit.getPlayer(args[0])
        if (player == null) {
            sender.sendMessage(plugin.messages.noPlayerFound)
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
        val characterLanguageService = Services[RPKCharacterLanguageService::class.java]
        if (characterLanguageService == null) {
            sender.sendMessage(plugin.messages.noCharacterLanguageService)
            return true
        }
        val languageService = Services[RPKLanguageService::class.java]
        if (languageService == null) {
            sender.sendMessage(plugin.messages.noLanguageService)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return true
        }
        sender.sendMessage(
            plugin.messages.listCharacterLanguageUnderstandingTitle.withParameters(
                player,
                character
            )
        )
        languageService.languages.forEach {
            sender.sendMessage(
                plugin.messages.listCharacterLanguageUnderstandingItem.withParameters(
                    it,
                    characterLanguageService.getCharacterLanguageUnderstanding(character, it).get()
                )
            )
        }
        return true
    }
}
