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

package com.rpkit.characters.bukkit.command.character.set

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.command.result.NoCharacterSelfFailure
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import kotlin.math.max
import kotlin.math.min

/**
 * Character set age command.
 * Sets character's age.
 */
class CharacterSetAgeCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(AgePrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(plugin.messages.notFromConsole)
                .addConversationAbandonedListener { event ->
                    if (!event.gracefulExit()) {
                        val conversable = event.context.forWhom
                        if (conversable is Player) {
                            conversable.sendMessage(plugin.messages.operationCancelled)
                        }
                    }
                }
    }

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        if (!sender.hasPermission("rpkit.characters.command.character.set.age")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetAge)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.set.age"))
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
        val character = characterService.getPreloadedActiveCharacter(sender)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return completedFuture(NoCharacterSelfFailure())
        }
        if (args.isEmpty()) {
            val bukkitPlayer = sender.toBukkitPlayer()
            if (bukkitPlayer != null) {
                conversationFactory.buildConversation(bukkitPlayer).begin()
            }
            return completedFuture(CommandSuccess)
        }
        try {
            val age = args[0].toInt()
            val minAge = max(plugin.config.getInt("characters.min-age"), character.species?.minAge ?: Int.MIN_VALUE)
            val maxAge = min(plugin.config.getInt("characters.max-age"), character.species?.maxAge ?: Int.MAX_VALUE)
            return if (age in minAge..maxAge) {
                character.age = age
                characterService.updateCharacter(character).thenApply { updatedCharacter ->
                    sender.sendMessage(plugin.messages.characterSetAgeValid)
                    updatedCharacter?.showCharacterCard(sender)
                    CommandSuccess
                }
            } else {
                sender.sendMessage(plugin.messages.characterSetAgeInvalidValidation.withParameters(
                    minAge = minAge,
                    maxAge = maxAge
                ))
                completedFuture(IncorrectUsageFailure())
            }
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages.characterSetAgeInvalidNumber)
            return completedFuture(IncorrectUsageFailure())
        }
    }

    private inner class AgePrompt : NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetAgePrompt
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
            context.setSessionData("minecraftProfileService", minecraftProfileService)
            val characterService = Services[RPKCharacterService::class.java] ?: return false
            context.setSessionData("characterService", characterService)
            val conversable = context.forWhom
            if (conversable !is Player) return false
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
            context.setSessionData("minecraftProfile", minecraftProfile)
            if (minecraftProfile == null) return false
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
            context.setSessionData("character", character)
            val minAge = max(plugin.config.getInt("characters.min-age"), character?.species?.minAge ?: Int.MIN_VALUE)
            val maxAge = min(plugin.config.getInt("characters.max-age"), character?.species?.maxAge ?: Int.MAX_VALUE)
            return input.toInt() in minAge..maxAge
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            if (context.getSessionData("minecraftProfileService") == null) return plugin.messages.noMinecraftProfileService
            if (context.getSessionData("characterService") == null) return plugin.messages.noCharacterService
            if (context.getSessionData("minecraftProfile") == null) return plugin.messages.noMinecraftProfile
            val character = context.getSessionData("character") as? RPKCharacter ?: return plugin.messages.noCharacter
            val minAge = max(plugin.config.getInt("characters.min-age"), character.species?.minAge ?: Int.MIN_VALUE)
            val maxAge = min(plugin.config.getInt("characters.max-age"), character.species?.maxAge ?: Int.MAX_VALUE)
            return plugin.messages.characterSetAgeInvalidValidation.withParameters(
                minAge = minAge,
                maxAge = maxAge
            )
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.characterSetAgeInvalidNumber
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            val characterService = context.getSessionData("characterService") as RPKCharacterService
            val character = context.getSessionData("character") as RPKCharacter
            character.age = input.toInt()
            characterService.updateCharacter(character)
            return AgeSetPrompt()
        }

    }

    private inner class AgeSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val minecraftProfile = context.getSessionData("minecraftProfile") as RPKMinecraftProfile
            val character = context.getSessionData("character") as RPKCharacter
            character.showCharacterCard(minecraftProfile)
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetAgeValid
        }

    }

}
