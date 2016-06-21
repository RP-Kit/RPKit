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

package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class CharacterSetGenderCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(GenderPrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
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
            if (sender.hasPermission("elysium.characters.command.character.set.gender")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    if (args.size > 0) {
                        val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class.java)
                        val gender = genderProvider.getGender(args[0])
                        if (gender != null) {
                            character.gender = gender
                            characterProvider.updateCharacter(character)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-valid")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-invalid-gender")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages-no-permission-character-set-gender")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class GenderPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class.java).getGender(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    character.gender = genderProvider.getGender(input)!!
                    characterProvider.updateCharacter(character)
                }
            }
            return GenderSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-invalid-gender"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class.java)
            val genderListBuilder = StringBuilder()
            for (gender in genderProvider.genders) {
                genderListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-item")
                        .replace("\$gender", gender.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-prompt")) + "\n" + genderListBuilder.toString()
        }

    }

    private inner class GenderSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-valid"))
        }

    }

}
