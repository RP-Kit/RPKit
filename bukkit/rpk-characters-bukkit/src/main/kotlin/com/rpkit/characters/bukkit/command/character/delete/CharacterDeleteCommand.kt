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

package com.rpkit.characters.bukkit.command.character.delete

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
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
    private val conversationFactory: ConversationFactory
    private val confirmationConversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(CharacterPrompt())
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
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.delete")) {
            sender.sendMessage(plugin.messages["no-permission-character-delete"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        val characterName = args.joinToString(" ")
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            if (profile is RPKProfile) {
                var charFound = false
                // Prioritise exact matches...
                for (character in characterService.getCharacters(profile)) {
                    if (character.name.equals(characterName, ignoreCase = true)) {
                        val conversation = confirmationConversationFactory.buildConversation(sender)
                        conversation.context.setSessionData("character_id", character.id)
                        conversation.begin()
                        charFound = true
                        break
                    }
                }
                // And fall back to partial matches
                if (!charFound) {
                    for (character in characterService.getCharacters(profile)) {
                        if (character.name.toLowerCase().contains(characterName.toLowerCase())) {
                            val conversation = confirmationConversationFactory.buildConversation(sender)
                            conversation.context.setSessionData("character_id", character.id)
                            conversation.begin()
                            charFound = true
                            break
                        }
                    }
                }
                if (charFound) {
                    sender.sendMessage(plugin.messages["character-delete-valid"])
                } else {
                    sender.sendMessage(plugin.messages["character-delete-invalid-character"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-profile"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
        }
        return true
    }

    private inner class CharacterPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val conversable = context.forWhom
            if (conversable !is Player) return false
            val characterService = Services[RPKCharacterService::class] ?: return false
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return false
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
            if (minecraftProfile == null) return false
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) return false
            if (characterService.getCharacters(profile).any { it.name.equals(input, ignoreCase = true) }) {
                return true
            }
            if (characterService.getCharacters(profile).any { it.name.toLowerCase().contains(input.toLowerCase()) }) {
                return true
            }
            return false
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) {
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class]
            if (characterService == null) {
                return END_OF_CONVERSATION
            }
            val minecraftProfileService = Services[RPKMinecraftProfileService::class]
            if (minecraftProfileService == null) {
                return END_OF_CONVERSATION
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
            if (minecraftProfile == null) {
                return END_OF_CONVERSATION
            }
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) {
                return END_OF_CONVERSATION
            }
            var charFound = false
            // Prioritise exact matches...
            for (character in characterService.getCharacters(profile)) {
                if (character.name.equals(input, ignoreCase = true)) {
                    context.setSessionData("character_id", character.id)
                    charFound = true
                    break
                }
            }
            // And fall back to partial matches
            if (!charFound) {
                for (character in characterService.getCharacters(profile)) {
                    if (character.name.toLowerCase().contains(input.toLowerCase())) {
                        context.setSessionData("character_id", character.id)
                        break
                    }
                }
            }
            return ConfirmationPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            val conversable = context.forWhom
            if (conversable !is Player) return plugin.messages["not-from-console"]
            val characterService = Services[RPKCharacterService::class]
            if (characterService == null) return plugin.messages["no-character-service"]
            val minecraftProfileService = Services[RPKMinecraftProfileService::class]
            if (minecraftProfileService == null) return plugin.messages["no-minecraft-profile-service"]
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
            if (minecraftProfile == null) return plugin.messages["no-minecraft-profile"]
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) return plugin.messages["no-profile"]
            return plugin.messages["character-delete-invalid-character"]
        }

        override fun getPromptText(context: ConversationContext): String {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return plugin.messages["no-minecraft-profile-service"]
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(context.forWhom as Player)
                    ?: return plugin.messages["no-minecraft-profile"]
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) {
                return plugin.messages["no-profile"]
            }
            val characterService = Services[RPKCharacterService::class] ?: return plugin.messages["no-character-service"]
            val characterListBuilder = StringBuilder()
            for (character in characterService.getCharacters(profile)) {
                characterListBuilder.append("\n").append(
                        plugin.messages["character-list-item", mapOf(
                                Pair("character", character.name))]
                )
            }
            return plugin.messages["character-delete-prompt"] + characterListBuilder.toString()
        }

    }

    private inner class ConfirmationPrompt : BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt? {
            if (!input) {
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class] ?: return END_OF_CONVERSATION
            val character = characterService.getCharacter(context.getSessionData("character_id") as Int)
            if (character != null) {
                characterService.removeCharacter(character)
            }
            return CharacterDeletedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["character-delete-confirmation-invalid-boolean"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-delete-confirmation"]
        }

    }

    private inner class CharacterDeletedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-delete-valid"]
        }
    }

}
