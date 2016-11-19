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

package com.seventh_root.elysium.characters.bukkit.command.character.set

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character set age command.
 * Sets character's age.
 */
class CharacterSetAgeCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(AgePrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.characters.command.character.set.age")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    if (args.size > 0) {
                        try {
                            val age = args[0].toInt()
                            if (age >= plugin.config.getInt("characters.min-age") && age <= plugin.config.getInt("characters.max-age")) {
                                character.age = age
                                characterProvider.updateCharacter(character)
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-valid")))
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-invalid-validation")))
                            }
                        } catch (exception: NumberFormatException) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-invalid-number")))
                        }

                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-set-age")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class AgePrompt: NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-prompt"))
        }

        override fun isNumberValid(context: ConversationContext?, input: Number?): Boolean {
            return input!!.toInt() >= plugin.config.getInt("characters.min-age") && input.toInt() <= plugin.config.getInt("characters.max-age")
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: Number?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-invalid-validation"))
        }

        override fun getInputNotNumericText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-invalid-number"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val player = playerProvider.getPlayer(conversable)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    character.age = input.toInt()
                    characterProvider.updateCharacter(character)
                }
            }
            return AgeSetPrompt()
        }

    }

    private inner class AgeSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-valid"))
        }

    }

}
