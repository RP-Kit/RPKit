/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character set profile command.
 * Transfers a character to another profile.
 */
class CharacterSetProfileCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

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
        if (sender is Player) {
            if (sender.hasPermission("rpkit.characters.command.character.set.profile")) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        if (args.isNotEmpty()) {
                            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                            val newProfile = profileProvider.getProfile(args[0])
                            if (newProfile != null) {
                                character.profile = newProfile
                                characterProvider.updateCharacter(character)
                                characterProvider.setActiveCharacter(minecraftProfile, null)
                                sender.sendMessage(plugin.messages["character-set-profile-valid"])
                                character.showCharacterCard(minecraftProfile)
                            } else {
                                sender.sendMessage(plugin.messages["character-set-profile-invalid-profile"])
                            }
                        } else {
                            conversationFactory.buildConversation(sender).begin()
                        }
                    } else {
                        sender.sendMessage(plugin.messages["no-character"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-character-set-player"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

    private inner class ProfilePrompt: ValidatingPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-profile-prompt"]
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(conversable)
                if (minecraftProfile != null) {
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                        val newProfile = profileProvider.getProfile(input)
                        if (newProfile != null) {
                            character.profile = newProfile
                            characterProvider.updateCharacter(character)
                            characterProvider.setActiveCharacter(minecraftProfile, null)
                        }
                    }
                }
            }
            return ProfileSetPrompt()
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val conversable = context.forWhom as? Player ?: return false
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(conversable) ?: return false
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            characterProvider.getActiveCharacter(minecraftProfile) ?: return false
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            profileProvider.getProfile(input) ?: return false
            return true
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            val conversable = context.forWhom as? Player ?: return plugin.messages["not-from-console"]
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(conversable) ?: return plugin.messages["no-minecraft-profile"]
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            characterProvider.getActiveCharacter(minecraftProfile) ?: return plugin.messages["no-character"]
            return plugin.messages["character-set-profile-invalid-profile"]
        }

    }

    private inner class ProfileSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(context.forWhom as Player)
                if (minecraftProfile != null) {
                    characterProvider.getActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
                }
            }
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-profile-valid"]
        }

    }
}
