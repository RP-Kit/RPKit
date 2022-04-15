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

package com.rpkit.languages.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.command.result.NoCharacterOtherFailure
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguageService
import com.rpkit.languages.bukkit.language.RPKLanguageService
import com.rpkit.players.bukkit.command.result.NoMinecraftProfileOtherFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class LanguageListUnderstandingCommand(private val plugin: RPKLanguagesBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.languages.command.language.listunderstanding")) {
            sender.sendMessage(plugin.messages.noPermissionLanguageListUnderstanding)
            return completedFuture(NoPermissionFailure("rpkit.languages.command.language.listunderstanding"))
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.languageListUnderstandingUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        val player = plugin.server.getPlayer(args[0])
        if (player == null) {
            sender.sendMessage(plugin.messages.noPlayerFound)
            return completedFuture(InvalidTargetFailure())
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return completedFuture(MissingServiceFailure(RPKCharacterService::class.java))
        }
        val characterLanguageService = Services[RPKCharacterLanguageService::class.java]
        if (characterLanguageService == null) {
            sender.sendMessage(plugin.messages.noCharacterLanguageService)
            return completedFuture(MissingServiceFailure(RPKCharacterLanguageService::class.java))
        }
        val languageService = Services[RPKLanguageService::class.java]
        if (languageService == null) {
            sender.sendMessage(plugin.messages.noLanguageService)
            return completedFuture(MissingServiceFailure(RPKLanguageService::class.java))
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return completedFuture(NoMinecraftProfileOtherFailure())
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return completedFuture(NoCharacterOtherFailure())
        }
        sender.sendMessage(
            plugin.messages.languageListUnderstandingTitle.withParameters(
                player,
                character
            )
        )
        languageService.languages.forEach {
            sender.sendMessage(
                plugin.messages.languageListUnderstandingItem.withParameters(
                    it,
                    characterLanguageService.getCharacterLanguageUnderstanding(character, it).get()
                )
            )
        }
        return completedFuture(CommandSuccess)
    }

    class InvalidTargetFailure : CommandFailure()
}
