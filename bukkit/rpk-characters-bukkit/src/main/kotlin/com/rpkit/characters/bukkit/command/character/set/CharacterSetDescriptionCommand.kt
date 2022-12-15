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
 * Character set description command.
 * Sets character's description state.
 */
class CharacterSetDescriptionCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(DescriptionPrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.description")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetDescription)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.set.description"))
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
        val descriptionBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            descriptionBuilder.append(args[i]).append(" ")
        }
        descriptionBuilder.append(args[args.size - 1])
        character.description = descriptionBuilder.toString()
        return characterService.updateCharacter(character).thenApply { updatedCharacter ->
            sender.sendMessage(plugin.messages.characterSetDescriptionValid)
            updatedCharacter?.showCharacterCard(sender)
            CommandSuccess
        }
    }

    private inner class DescriptionPrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetDescriptionPrompt
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt {
            if (context.getSessionData("description") == null) {
                context.setSessionData("description", "")
            }
            if (!input.equals("end", ignoreCase = true)) {
                val previousDescription = context.getSessionData("description") as String
                context.setSessionData("description", "$previousDescription $input")
                return DescriptionPrompt()
            }
            val conversable = context.forWhom
            if (conversable !is Player) return DescriptionSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return DescriptionSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return DescriptionSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
                ?: return DescriptionSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return DescriptionSetPrompt()
            character.description = context.getSessionData("description") as String
            characterService.updateCharacter(character)
            return DescriptionSetPrompt()
        }

    }

    private inner class DescriptionSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
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
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetDescriptionValid
        }

    }
}
