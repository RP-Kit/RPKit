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
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character set gender command.
 * Sets character's gender.
 */
class CharacterSetGenderCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(GenderPrompt())
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
            if (sender.hasPermission("rpkit.characters.command.character.set.gender")) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        if (args.isNotEmpty()) {
                            val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
                            val gender = genderProvider.getGender(args[0])
                            if (gender != null) {
                                character.gender = gender
                                characterProvider.updateCharacter(character)
                                sender.sendMessage(plugin.messages["character-set-gender-valid"])
                                character.showCharacterCard(minecraftProfile)
                            } else {
                                sender.sendMessage(plugin.messages["character-set-gender-invalid-gender"])
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
                sender.sendMessage(plugin.messages["no-permission-character-set-gender"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

    private inner class GenderPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class).getGender(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(conversable)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        character.gender = genderProvider.getGender(input)!!
                        characterProvider.updateCharacter(character)
                    }
                }
            }
            return GenderSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return plugin.messages["character-set-gender-invalid-gender"]
        }

        override fun getPromptText(context: ConversationContext): String {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
            val genderListBuilder = StringBuilder()
            for (gender in genderProvider.genders) {
                genderListBuilder.append(plugin.messages["gender-list-item", mapOf(
                        Pair("gender", gender.name)
                )]).append("\n")
            }
            return plugin.messages["character-set-gender-prompt"] + "\n" + genderListBuilder.toString()
        }

    }

    private inner class GenderSetPrompt: MessagePrompt() {

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
            return plugin.messages["character-set-gender-valid"]
        }

    }

}
