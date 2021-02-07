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
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.MessagePrompt
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.entity.Player

/**
 * Character switch command.
 * Switches active character.
 */
class CharacterSwitchCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

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
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.switch")) {
            sender.sendMessage(plugin.messages["no-permission-character-switch"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        val characterNameBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            characterNameBuilder.append(args[i]).append(" ")
        }
        characterNameBuilder.append(args[args.size - 1])
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        var charFound = false
        var unplayableOtherAccount = false // Has a character been skipped due to already being played on another account?
        // Prioritise exact matches...
        characterService.getCharacters(profile)
                .filter { it.name.equals(characterNameBuilder.toString(), ignoreCase = true) }
                .forEach {
                    if (it.minecraftProfile == null) {
                        characterService.setActiveCharacter(minecraftProfile, it)
                        charFound = true
                    } else {
                        unplayableOtherAccount = true
                    }
                }
        // And fall back to partial matches
        if (!charFound) {
            characterService.getCharacters(profile)
                    .filter { it.name.toLowerCase().contains(characterNameBuilder.toString().toLowerCase()) }
                    .forEach {
                        if (it.minecraftProfile == null) {
                            characterService.setActiveCharacter(minecraftProfile, it)
                            charFound = true
                        } else {
                            unplayableOtherAccount = true
                        }
                    }
        }
        if (charFound) {
            sender.sendMessage(plugin.messages["character-switch-valid"])
        } else {
            if (unplayableOtherAccount) {
                sender.sendMessage(plugin.messages["character-switch-invalid-character-other-account"])
            } else {
                sender.sendMessage(plugin.messages["character-switch-invalid-character"])
            }
        }
        return true
    }

    private inner class CharacterPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val conversable = context.forWhom
            if (conversable !is Player) return false
            val characterService = Services[RPKCharacterService::class.java] ?: return false
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
            if (minecraftProfile == null) return false
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) return false
            if (characterService.getCharacters(profile)
                    .filter { it.name.equals(input, ignoreCase = true) }
                    .any { it.minecraftProfile == null }) { return true }
            if (characterService.getCharacters(profile)
                    .filter { it.name.toLowerCase().contains(input.toLowerCase()) }
                    .any { it.minecraftProfile == null }) { return true }
            return false
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) {
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                conversable.sendMessage(plugin.messages["no-character-service"])
                return END_OF_CONVERSATION
            }
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                conversable.sendMessage(plugin.messages["no-minecraft-profile-service"])
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
            characterService.getCharacters(profile)
                    .filter { it.name.equals(input, ignoreCase = true) }
                    .forEach {
                        if (it.minecraftProfile == null) {
                            characterService.setActiveCharacter(minecraftProfile, it)
                            charFound = true
                        }
                    }
            // And fall back to partial matches
            if (!charFound) {
                characterService.getCharacters(profile)
                        .filter { it.name.toLowerCase().contains(input.toLowerCase()) }
                        .forEach {
                            if (it.minecraftProfile == null) {
                                characterService.setActiveCharacter(minecraftProfile, it)
                                charFound = true
                            }
                        }
            }
            return CharacterSwitchedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            val conversable = context.forWhom
            if (conversable !is Player) return plugin.messages["character-switch-invalid-character"]
            val characterService = Services[RPKCharacterService::class.java] ?: return plugin.messages["no-character-service"]
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                    ?: return plugin.messages["no-minecraft-profile-service"]
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
                    ?: return plugin.messages["character-switch-invalid-character"]
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) return plugin.messages["character-switch-invalid-character"]
            var charFound = false
            // Prioritise exact matches...
            characterService.getCharacters(profile)
                    .filter { it.name.equals(invalidInput, ignoreCase = true) }
                    .forEach {
                        charFound = true
                        if (it.minecraftProfile != null) {
                            return plugin.messages["character-switch-invalid-character-other-account"]
                        }
                    }
            // And fall back to partial matches
            if (!charFound) {
                characterService.getCharacters(profile)
                        .filter { it.name.toLowerCase().contains(invalidInput.toLowerCase()) }
                        .forEach {
                            if (it.minecraftProfile != null) {
                                return plugin.messages["character-switch-invalid-character-other-account"]
                            }
                        }
            }
            return plugin.messages["character-switch-invalid-character"]
        }

        override fun getPromptText(context: ConversationContext): String {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                    ?: return plugin.messages["no-minecraft-profile-service"]
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(context.forWhom as Player)
                    ?: return plugin.messages["no-minecraft-profile"]
            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) {
                return plugin.messages["no-profile"]
            }
            val characterService = Services[RPKCharacterService::class.java]
                    ?: return plugin.messages["no-character-service"]
            val characterListBuilder = StringBuilder()
            for (character in characterService.getCharacters(profile)) {
                characterListBuilder.append("\n").append(
                        plugin.messages["character-list-item", mapOf(
                            "character" to character.name
                        )])
            }
            return plugin.messages["character-switch-prompt"] + characterListBuilder.toString()
        }

    }

    private inner class CharacterSwitchedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-switch-valid"]
        }
    }

}
