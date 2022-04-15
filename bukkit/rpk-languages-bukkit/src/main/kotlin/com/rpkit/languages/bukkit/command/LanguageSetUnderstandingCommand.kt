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
import com.rpkit.languages.bukkit.language.RPKLanguageName
import com.rpkit.languages.bukkit.language.RPKLanguageService
import com.rpkit.players.bukkit.command.result.NoMinecraftProfileSelfFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class LanguageSetUnderstandingCommand(private val plugin: RPKLanguagesBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (args.size < 3) {
            sender.sendMessage(plugin.messages.languageSetUnderstandingUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        val target = plugin.server.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage(plugin.messages.languageSetUnderstandingInvalidTarget)
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
        val languageService = Services[RPKLanguageService::class.java]
        if (languageService == null) {
            sender.sendMessage(plugin.messages.noLanguageService)
            return completedFuture(MissingServiceFailure(RPKLanguageService::class.java))
        }
        val language = languageService.getLanguage(
            RPKLanguageName(args.drop(1).dropLast(1).joinToString(" "))
        )
        if (language == null) {
            sender.sendMessage(plugin.messages.languageSetUnderstandingInvalidLanguage)
            return completedFuture(InvalidLanguageFailure())
        }
        val characterLanguageService = Services[RPKCharacterLanguageService::class.java]
        if (characterLanguageService == null) {
            sender.sendMessage(plugin.messages.noCharacterLanguageService)
            return completedFuture(MissingServiceFailure(RPKCharacterLanguageService::class.java))
        }
        val understanding = try {
            args.last().toFloat()
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages.languageSetUnderstandingInvalidUnderstanding)
            return completedFuture(InvalidUnderstandingFailure())
        }
        return minecraftProfileService.getMinecraftProfile(target).thenApplyAsync getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) {
                sender.sendMessage(plugin.messages.noMinecraftProfile)
                return@getMinecraftProfile NoMinecraftProfileSelfFailure()
            }
            characterService.getActiveCharacter(minecraftProfile).thenApply getActiveCharacter@{ character ->
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getActiveCharacter NoCharacterOtherFailure()
                }
                characterLanguageService.setCharacterLanguageUnderstanding(character, language, understanding).join()
                sender.sendMessage(plugin.messages.languageSetUnderstandingValid.withParameters(
                    character = character,
                    language = language,
                    understanding = understanding
                ))
                return@getActiveCharacter CommandSuccess
            }.join()
        }
    }

    class InvalidTargetFailure : CommandFailure()
    class InvalidLanguageFailure : CommandFailure()
    class InvalidUnderstandingFailure : CommandFailure()
}