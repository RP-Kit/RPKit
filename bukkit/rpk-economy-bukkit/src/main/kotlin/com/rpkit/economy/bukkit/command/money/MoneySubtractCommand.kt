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

package com.rpkit.economy.bukkit.command.money

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Money subtract command.
 * Takes money from a player's active character.
 */
class MoneySubtractCommand(val plugin: RPKEconomyBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.economy.command.money.subtract")) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                if (args.isNotEmpty()) {
                    val bukkitPlayer = plugin.server.getPlayer(args[0])
                    if (bukkitPlayer != null) {
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                        if (minecraftProfile != null) {
                            val profile = minecraftProfile.profile
                            if (profile != null) {
                                if (args.size > 1) {
                                    val character = characterProvider.getCharacters(profile)
                                            .firstOrNull { character -> character.name.startsWith(args[1]) }
                                    if (character != null) {
                                        if (args.size > 2) {
                                            val currency = currencyProvider.getCurrency(args[2])
                                            if (currency != null) {
                                                if (args.size > 3) {
                                                    try {
                                                        val amount = args[3].toInt()
                                                        if (amount >= 0) {
                                                            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
                                                            sender.sendMessage(plugin.messages["money-subtract-amount-valid"])
                                                            sender.sendMessage(plugin.messages["money-subtract-valid"])
                                                        } else {
                                                            sender.sendMessage(plugin.messages["money-subtract-value-invalid-value-negative"])
                                                        }
                                                    } catch (exception: NumberFormatException) {
                                                        sender.sendMessage(plugin.messages["money-subtract-value-invalid-value-number"])
                                                    }
                                                } else {
                                                    conversationFactory.buildConversation(sender).begin()
                                                }
                                            } else {
                                                sender.sendMessage(plugin.messages["money-subtract-currency-invalid-currency"])
                                            }
                                        } else {
                                            conversationFactory.buildConversation(sender).begin()
                                        }
                                    } else {
                                        sender.sendMessage(plugin.messages["money-subtract-character-invalid-character"])
                                    }
                                } else {
                                    conversationFactory.buildConversation(sender).begin()
                                }
                            } else {
                                sender.sendMessage(plugin.messages["no-profile"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-profile"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["money-subtract-profile-invalid-profile"])
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-money-subtract"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

    private inner class ProfilePrompt: ValidatingPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profile = profileProvider.getProfile(input)
            context.setSessionData("profile", profile)
            return ProfileSetPrompt()
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            return profileProvider.getProfile(input) != null
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-profile-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-subtract-profile-invalid-profile"]
        }

    }

    private inner class ProfileSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-profile-valid"]
        }

    }

    private inner class CharacterPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    .getCharacters(context.getSessionData("profile") as RPKProfile)
                    .any { character -> character.name == input }
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("character", plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    .getCharacters(context.getSessionData("profile") as RPKProfile)
                    .first { character -> character.name == input }
            )
            return CharacterSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-character-prompt"] +
                    "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                            .getCharacters(context.getSessionData("profile") as RPKProfile)
                            .joinToString("\n") { character ->
                                plugin.messages["money-subtract-character-prompt-list-item", mapOf(
                                        Pair("character", character.name)
                                )]
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-subtract-character-invalid-character"]
        }
    }

    private inner class CharacterSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-character-valid"]
        }

    }

    private inner class CurrencyPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-currency-prompt"] + "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).currencies
                            .joinToString("\n") { currency ->
                                plugin.messages["money-subtract-currency-prompt-list-item", mapOf(
                                        Pair("currency", currency.name)
                                )]
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-subtract-currency-invalid-currency"]
        }
    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-currency-valid"]
        }

    }

    private inner class AmountPrompt: NumericPrompt() {

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val balance = economyProvider.getBalance(context.getSessionData("character") as RPKCharacter, context.getSessionData("currency") as RPKCurrency)
            return input.toInt() in 1..balance
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("amount", input.toInt())
            return AmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-amount-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return if (invalidInput.toInt() <= 0) {
                plugin.messages["money-subtract-amount-invalid-amount-negative"]
            } else {
                plugin.messages["money-subtract-amount-invalid-amount-balance"]
            }
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-subtract-amount-invalid-amount-number"]
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneySubtractCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-amount-valid"]
        }

    }

    private inner class MoneySubtractCompletePrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val character = context.getSessionData("character") as RPKCharacter
            val currency = context.getSessionData("currency") as RPKCurrency
            val amount = context.getSessionData("amount") as Int
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
            return plugin.messages["money-subtract-valid"]
        }

    }

}