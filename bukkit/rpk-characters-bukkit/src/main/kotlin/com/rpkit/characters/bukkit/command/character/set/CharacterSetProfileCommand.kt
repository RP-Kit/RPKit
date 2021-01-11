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

package com.rpkit.characters.bukkit.command.character.set

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
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
 * Character set profile command.
 * Transfers a character to another profile.
 */
class CharacterSetProfileCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(ProfilePrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.profile")) {
            sender.sendMessage(plugin.messages["no-permission-character-set-player"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
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
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages["no-profile-service"])
            return true
        }
        if (!args[0].contains("#")) {
            sender.sendMessage(plugin.messages["character-set-profile-invalid-no-discriminator"])
            return true
        }
        val (name, discriminatorString) = args[0].split("#")
        val discriminator = discriminatorString.toIntOrNull()
        if (discriminator == null) {
            sender.sendMessage(plugin.messages["character-set-profile-invalid-discriminator"])
            return true
        }
        val newProfile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator))
        if (newProfile == null) {
            sender.sendMessage(plugin.messages["character-set-profile-invalid-profile"])
            return true
        }
        character.profile = newProfile
        characterService.updateCharacter(character)
        characterService.setActiveCharacter(minecraftProfile, null)
        sender.sendMessage(plugin.messages["character-set-profile-valid"])
        character.showCharacterCard(minecraftProfile)
        return true
    }

    private inner class ProfilePrompt : ValidatingPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-profile-prompt"]
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) return END_OF_CONVERSATION
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return END_OF_CONVERSATION
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable) ?: return END_OF_CONVERSATION
            val characterService = Services[RPKCharacterService::class.java] ?: return END_OF_CONVERSATION
            val character = characterService.getActiveCharacter(minecraftProfile) ?: return END_OF_CONVERSATION
            val profileService = Services[RPKProfileService::class.java] ?: return END_OF_CONVERSATION
            val (name, discriminatorString) = input.split("#")
            val discriminator = discriminatorString.toIntOrNull() ?: return END_OF_CONVERSATION
            val newProfile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return END_OF_CONVERSATION
            character.profile = newProfile
            characterService.updateCharacter(character)
            characterService.setActiveCharacter(minecraftProfile, null)
            return ProfileSetPrompt()
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val conversable = context.forWhom as? Player ?: return false
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable) ?: return false
            val characterService = Services[RPKCharacterService::class.java] ?: return false
            characterService.getActiveCharacter(minecraftProfile) ?: return false
            val profileService = Services[RPKProfileService::class.java] ?: return false
            if (!input.contains("#")) return false
            val (name, discriminatorString) = input.split("#")
            val discriminator = discriminatorString.toIntOrNull() ?: return false
            profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return false
            return true
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            val conversable = context.forWhom as? Player ?: return plugin.messages["not-from-console"]
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return plugin.messages["no-minecraft-profile-service"]
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(conversable)
                    ?: return plugin.messages["no-minecraft-profile"]
            val characterService = Services[RPKCharacterService::class.java] ?: return plugin.messages["no-character-service"]
            characterService.getActiveCharacter(minecraftProfile) ?: return plugin.messages["no-character"]
            val profileService = Services[RPKProfileService::class.java] ?: return plugin.messages["character-set-profile-invalid-profile"]
            if (!invalidInput.contains("#")) return plugin.messages["character-set-profile-invalid-no-discriminator"]
            val (name, discriminatorString) = invalidInput.split("#")
            val discriminator = discriminatorString.toIntOrNull() ?: return plugin.messages["character-set-profile-invalid-discriminator"]
            profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return plugin.messages["character-set-profile-invalid-profile"]
            return ""
        }

    }

    private inner class ProfileSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return Prompt.END_OF_CONVERSATION
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                conversable.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                conversable.sendMessage(plugin.messages["no-character-service"])
                return END_OF_CONVERSATION
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(context.forWhom as Player)
            if (minecraftProfile != null) {
                characterService.getActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-profile-valid"]
        }

    }
}
