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
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.command.result.NoCharacterSelfFailure
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
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

/**
 * Character set gender command.
 * Sets character's gender.
 */
class CharacterSetGenderCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {
    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(GenderPrompt())
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

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        if (!sender.hasPermission("rpkit.characters.command.character.set.gender")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetGender)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.set.gender"))
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
        character.gender = args.joinToString(" ")
        return characterService.updateCharacter(character).thenApply { updatedCharacter ->
            sender.sendMessage(plugin.messages.characterSetGenderValid)
            updatedCharacter?.showCharacterCard(sender)
            CommandSuccess
        }
    }

    private inner class GenderPrompt : StringPrompt() {

        override fun acceptInput(context: ConversationContext, input: String?): Prompt {
            if (input == null) {
                return GenderNotSetPrompt()
            }
            val conversable = context.forWhom
            if (conversable !is Player) return GenderSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return GenderNotSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return GenderNotSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return GenderNotSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return GenderNotSetPrompt()
            character.gender = input
            characterService.updateCharacter(character)
            return GenderSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetGenderPrompt
        }

    }

    private inner class GenderNotSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return END_OF_CONVERSATION
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                conversable.sendMessage(plugin.messages.noMinecraftProfileService)
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                conversable.sendMessage(plugin.messages.noCharacterService)
                return END_OF_CONVERSATION
            }
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(context.forWhom as Player)
            if (minecraftProfile != null) {
                characterService.getPreloadedActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetGenderNotSet
        }

    }

    private inner class GenderSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return END_OF_CONVERSATION
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                conversable.sendMessage(plugin.messages.noMinecraftProfileService)
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                conversable.sendMessage(plugin.messages.noCharacterService)
                return END_OF_CONVERSATION
            }
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(context.forWhom as Player)
            if (minecraftProfile != null) {
                characterService.getPreloadedActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetGenderValid
        }

    }

}
