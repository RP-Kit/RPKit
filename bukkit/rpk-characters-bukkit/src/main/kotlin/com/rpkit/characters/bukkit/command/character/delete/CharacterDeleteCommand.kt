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

package com.rpkit.characters.bukkit.command.character.delete

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.levenshtein
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character delete command.
 * Deletes a character.
 */
class CharacterDeleteCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    private val confirmationConversationFactory: ConversationFactory

    init {
        confirmationConversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(ConfirmationPrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(plugin.messages["not-from-console"])
                .addConversationAbandonedListener { event ->
                    if (!event.gracefulExit()) {
                        val conversable = event.context.forWhom
                        if (conversable is Player) {
                            conversable.sendMessage(plugin.messages["operation-cancelled"])
                        }
                    }
                }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.delete")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterDelete)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.characterDeleteUsage)
            return true
        }
        val characterName = args.joinToString(" ")
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
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            if (profile is RPKProfile) {
                characterService.getCharacters(profile).thenAccept { characters ->
                    val character = characters.minByOrNull { character -> character.name.levenshtein(characterName) }
                    if (character != null) {
                        val conversation = confirmationConversationFactory.buildConversation(sender)
                        conversation.context.setSessionData("character_id", character.id)
                    } else {
                        sender.sendMessage(plugin.messages.characterDeleteInvalidCharacter)
                    }
                }
            } else {
                sender.sendMessage(plugin.messages.noProfile)
            }
        } else {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
        }
        return true
    }

    private inner class ConfirmationPrompt : BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt? {
            if (!input) {
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class.java] ?: return END_OF_CONVERSATION
           characterService.getCharacter(context.getSessionData("character_id") as RPKCharacterId).thenAccept { character ->
                if (character != null) {
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        characterService.removeCharacter(character)
                    })
                }
            }
            return CharacterDeletedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.characterDeleteConfirmationInvalidBoolean
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterDeleteConfirmation
        }

    }

    private inner class CharacterDeletedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterDeleteValid
        }
    }

}
