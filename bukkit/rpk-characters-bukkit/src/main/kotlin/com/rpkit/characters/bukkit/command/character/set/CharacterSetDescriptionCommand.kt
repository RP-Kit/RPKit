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
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character set description command.
 * Sets character's description state.
 */
class CharacterSetDescriptionCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(DescriptionPrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(plugin.core.messages["not-from-console"])
                .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(plugin.core.messages["operation-cancelled"])
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.characters.command.character.set.description")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    if (args.isNotEmpty()) {
                        val descriptionBuilder = StringBuilder()
                        for (i in 0..args.size - 1 - 1) {
                            descriptionBuilder.append(args[i]).append(" ")
                        }
                        descriptionBuilder.append(args[args.size - 1])
                        character.description = descriptionBuilder.toString()
                        characterProvider.updateCharacter(character)
                        sender.sendMessage(plugin.core.messages["character-set-description-valid"])
                        character.showCharacterCard(player)
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["no-character"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["no-permission-character-set-description"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["not-from-console"])
        }
        return true
    }

    private inner class DescriptionPrompt: StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["character-set-description-prompt"]
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            if (context.getSessionData("description") == null) {
                context.setSessionData("description", "")
            }
            if (input.equals("end", ignoreCase = true)) {
                val conversable = context.forWhom
                if (conversable is Player) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val player = playerProvider.getPlayer(conversable)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        character.description = context.getSessionData("description") as String
                        characterProvider.updateCharacter(character)
                    }
                }
                return DescriptionSetPrompt()
            } else {
                val previousDescription = context.getSessionData("description") as String
                context.setSessionData("description", previousDescription + " " + input)
                return DescriptionPrompt()
            }
        }

    }

    private inner class DescriptionSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val player = playerProvider.getPlayer(context.forWhom as Player)
                characterProvider.getActiveCharacter(player)?.showCharacterCard(player)
            }
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["character-set-description-valid"]
        }

    }
}
