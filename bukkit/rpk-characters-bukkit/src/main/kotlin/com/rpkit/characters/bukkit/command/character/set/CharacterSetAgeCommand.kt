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

package com.rpkit.characters.bukkit.command.character.set

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character set age command.
 * Sets character's age.
 */
class CharacterSetAgeCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(AgePrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.age")) {
            sender.sendMessage(plugin.messages["no-permission-character-set-age"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        try {
            val age = args[0].toInt()
            if (age >= plugin.config.getInt("characters.min-age") && age <= plugin.config.getInt("characters.max-age")) {
                character.age = age
                characterService.updateCharacter(character)
                sender.sendMessage(plugin.messages["character-set-age-valid"])
                character.showCharacterCard(minecraftProfile)
            } else {
                sender.sendMessage(plugin.messages["character-set-age-invalid-validation"])
            }
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["character-set-age-invalid-number"])
        }
        return true
    }

    private inner class AgePrompt : NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-age-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return false
            context.setSessionData("minecraftProfileService", minecraftProfileService)
            val characterService = Services[RPKCharacterService::class] ?: return false
            context.setSessionData("characterService", characterService)
            val conversable = context.forWhom
            if (conversable !is Player) return false
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
            context.setSessionData("minecraftProfile", minecraftProfile)
            if (minecraftProfile == null) return false
            val character = characterService.getActiveCharacter(minecraftProfile)
            context.setSessionData("character", character)
            return input.toInt() >= plugin.config.getInt("characters.min-age")
                    && input.toInt() <= plugin.config.getInt("characters.max-age")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            if (context.getSessionData("minecraftProfileService") == null) return plugin.messages["no-minecraft-profile-service"]
            if (context.getSessionData("characterService") == null) return plugin.messages["no-character-service"]
            if (context.getSessionData("minecraftProfile") == null) return plugin.messages["no-minecraft-profile"]
            if (context.getSessionData("character") == null) return plugin.messages["no-character"]
            return plugin.messages["character-set-age-invalid-validation"]
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["character-set-age-invalid-number"]
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
            return plugin.messages["character-set-age-valid"]
        }

    }

}
