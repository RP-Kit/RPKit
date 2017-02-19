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
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Money set command.
 * Sets the amount of money a player's active character has.
 */
class MoneySetCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(PlayerPrompt())
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
    
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.economy.command.money.set")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                val fromBukkitPlayer = sender
                val fromPlayer = playerProvider.getPlayer(fromBukkitPlayer)
                val fromCharacter = characterProvider.getActiveCharacter(fromPlayer)
                if (fromCharacter != null) {
                    if (args.isNotEmpty()) {
                        val toBukkitPlayer = plugin.server.getPlayer(args[0])
                        if (toBukkitPlayer != null) {
                            val toPlayer = playerProvider.getPlayer(toBukkitPlayer)
                            if (args.size > 1) {
                                val character = characterProvider.getCharacters(toPlayer)
                                        .filter { character -> character.name.startsWith(args[1]) }
                                        .firstOrNull()
                                if (character != null) {
                                    if (args.size > 2) {
                                        val currency = currencyProvider.getCurrency(args[2])
                                        if (currency != null) {
                                            if (args.size > 3) {
                                                try {
                                                    val amount = args[3].toInt()
                                                    if (amount >= 0) {
                                                        if (amount <= 1728) {
                                                            economyProvider.setBalance(character, currency, amount)
                                                            sender.sendMessage(plugin.core.messages["money-set-amount-valid"])
                                                            sender.sendMessage(plugin.core.messages["money-set-valid"])
                                                        } else {
                                                            sender.sendMessage(plugin.core.messages["money-set-amount-invalid-amount-limit"])
                                                        }
                                                    } else {
                                                        sender.sendMessage(plugin.core.messages["money-set-amount-invalid-amount-negative"])
                                                    }
                                                } catch (exception: NumberFormatException) {
                                                    sender.sendMessage(plugin.core.messages["money-set-amount-invalid-amount-number"])
                                                }
                                            } else {
                                                conversationFactory.buildConversation(sender).begin()
                                            }
                                        } else {
                                            sender.sendMessage(plugin.core.messages["money-set-currency-invalid-currency"])
                                        }
                                    } else {
                                        conversationFactory.buildConversation(sender).begin()
                                    }
                                } else {
                                    sender.sendMessage(plugin.core.messages["money-set-character-invalid-character"])
                                }
                            } else {
                                conversationFactory.buildConversation(sender).begin()
                            }
                        } else {
                            sender.sendMessage(plugin.core.messages["money-set-player-invalid-player"])
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["no-character"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["no-permission-money-set"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["not-from-console"])
        }
        return true
    }

    private inner class PlayerPrompt: PlayerNamePrompt(plugin) {

        override fun acceptValidatedInput(context: ConversationContext, input: Player): Prompt {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val player = playerProvider.getPlayer(input)
            context.setSessionData("player", player)
            return PlayerSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-player-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.core.messages["money-set-player-invalid-player"]
        }

    }

    private inner class PlayerSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-player-valid"]
        }

    }

    private inner class CharacterPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    .getCharacters(context.getSessionData("player") as RPKPlayer)
                    .filter { character -> character.name == input }
                    .isNotEmpty()
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("character", plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    .getCharacters(context.getSessionData("player") as RPKPlayer)
                    .filter { character -> character.name == input }
                    .first()
            )
            return CharacterSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-character-prompt"] +
                    "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                            .getCharacters(context.getSessionData("player") as RPKPlayer)
                            .map { character -> plugin.core.messages["money-set-character-prompt-list-item", mapOf(
                                    Pair("character", character.name)
                            )] }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.core.messages["money-set-character-invalid-character"]
        }
    }

    private inner class CharacterSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-character-valid"]
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
            return plugin.core.messages["money-set-currency-prompt"] + "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).currencies
                            .map { currency -> plugin.core.messages["money-set-currency-prompt-list-item", mapOf(
                                    Pair("currency", currency.name)
                            )] }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.core.messages["money-set-currency-invalid-currency"]
        }
    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-currency-valid"]
        }

    }

    private inner class AmountPrompt: NumericPrompt() {

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0 && input.toInt() <= 1728
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("amount", input.toInt())
            return AmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-amount-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            if (invalidInput.toInt() < 0) {
                return plugin.core.messages["money-set-amount-invalid-amount-negative"]
            } else {
                return plugin.core.messages["money-set-amount-invalid-amount-limit"]
            }
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.core.messages["money-set-amount-invalid-amount-number"]
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneySetCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-amount-valid"]
        }

    }

    private inner class MoneySetCompletePrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val character = context.getSessionData("character") as RPKCharacter
            val currency = context.getSessionData("currency") as RPKCurrency
            val amount = context.getSessionData("amount") as Int
            economyProvider.setBalance(character, currency, amount)
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["money-set-valid"]
        }

    }
    
}