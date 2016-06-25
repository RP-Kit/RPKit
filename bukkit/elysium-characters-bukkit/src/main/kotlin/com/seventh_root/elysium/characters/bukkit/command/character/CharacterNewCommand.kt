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
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterImpl
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGender
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRace
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class CharacterNewCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(NamePrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
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
            if (sender.hasPermission("elysium.characters.command.character.new")) {
                conversationFactory.buildConversation(sender).begin()
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-new"))))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class NamePrompt: StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-name-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("name", input)
            return NameSetPrompt()
        }

    }

    private inner class NameSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return GenderPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-name-valid"))
        }

    }

    private inner class GenderPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class).getGender(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class)
            context.setSessionData("gender", genderProvider.getGender(input))
            return GenderSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-invalid-gender"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class)
            val genderListBuilder = StringBuilder()
            for (gender in genderProvider.genders) {
                genderListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-item")
                        .replace("\$gender", gender.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-prompt") + "\n" + genderListBuilder.toString())
        }

    }

    private inner class GenderSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AgePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-valid"))
        }

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
            context.setSessionData("age", input.toInt())
            return AgeSetPrompt()
        }

    }

    private inner class AgeSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return RacePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-valid"))
        }

    }

    private inner class RacePrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class).getRace(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class)
            context.setSessionData("race", raceProvider.getRace(input))
            return RaceSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-invalid-race"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class)
            val raceListBuilder = StringBuilder()
            for (race in raceProvider.races) {
                raceListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-item")
                        .replace("\$race", race.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-prompt")) + "\n" + raceListBuilder.toString()
        }

    }

    private inner class RaceSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return DescriptionPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-valid"))
        }

    }

    private inner class DescriptionPrompt: StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-description-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            if (context.getSessionData("description") == null) {
                context.setSessionData("description", "")
            }
            if (input.equals("end", ignoreCase = true)) {
                val conversable = context.forWhom
                if (conversable is Player) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
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
                context.setSessionData("description", (previousDescription + " ") + input)
                return DescriptionPrompt()
            }
        }

    }

    private inner class DescriptionSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterCreatedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-description-valid"))
        }

    }

    private inner class CharacterCreatedPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val conversable = context.forWhom
            if (conversable is Player) {
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val player = playerProvider.getPlayer(conversable)
                val newCharacter = ElysiumCharacterImpl(
                        plugin = plugin,
                        player = player,
                        name = context.getSessionData("name") as String,
                        gender = context.getSessionData("gender") as ElysiumGender,
                        age = context.getSessionData("age") as Int,
                        race = context.getSessionData("race") as ElysiumRace,
                        description = context.getSessionData("description") as String
                )
                characterProvider.addCharacter(newCharacter)
                characterProvider.setActiveCharacter(player, newCharacter)
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-new-valid"))
        }

    }

}
