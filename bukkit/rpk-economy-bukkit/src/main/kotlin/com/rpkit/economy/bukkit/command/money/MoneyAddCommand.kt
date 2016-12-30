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
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Money add command.
 * Gives money to a player's active character.
 */
class MoneyAddCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(PlayerPrompt())
            .withEscapeSequence("cancel")
            .thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.economy.command.money.add")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                if (args.size > 0) {
                    val bukkitPlayer = plugin.server.getPlayer(args[0])
                    if (bukkitPlayer != null) {
                        val player = playerProvider.getPlayer(bukkitPlayer)
                        if (args.size > 1) {
                            val character = characterProvider.getCharacters(player)
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
                                                    if (economyProvider.getBalance(character, currency) + amount <= 1728) {
                                                        economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + amount)
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-valid")))
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-valid")))
                                                    } else {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-invalid-amount-limit")))
                                                    }
                                                } else {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-value-invalid-value-negative")))
                                                }
                                            } catch (exception: NumberFormatException) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-value-invalid-value-number")))
                                            }
                                        } else {
                                            conversationFactory.buildConversation(sender).begin()
                                        }
                                    } else {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-currency-invalid-currency")))
                                    }
                                } else {
                                    conversationFactory.buildConversation(sender).begin()
                                }
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-character-invalid-character")))
                            }
                        } else {
                            conversationFactory.buildConversation(sender).begin()
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-player-invalid-player")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-money-add")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
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
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-player-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-player-invalid-player"))
        }

    }

    private inner class PlayerSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-player-valid"))
        }

    }

    private inner class CharacterPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    .getCharacters(context.getSessionData("player") as RPKPlayer)
                    .filter { character -> character.name.equals(input) }
                    .isNotEmpty()
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("character", plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    .getCharacters(context.getSessionData("player") as RPKPlayer)
                    .filter { character -> character.name.equals(input) }
                    .first()
            )
            return CharacterSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-character-prompt")) +
                    "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                            .getCharacters(context.getSessionData("player") as RPKPlayer)
                            .map { character -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-character-prompt-list-item")).replace("\$character", character.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-character-invalid-character"))
        }
    }

    private inner class CharacterSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-character-valid"))
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
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-currency-prompt")) + "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).currencies
                        .map { currency -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-currency-prompt-list-item")).replace("\$currency", currency.name) }
                        .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-currency-invalid-currency"))
        }
    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-currency-valid"))
        }

    }

    private inner class AmountPrompt: NumericPrompt() {

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() > 0
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("amount", input.toInt())
            return AmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-invalid-amount-negative"))
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-invalid-amount-number"))
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneyAddCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-valid"))
        }

    }

    private inner class MoneyAddCompletePrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val character = context.getSessionData("character") as RPKCharacter
            val currency = context.getSessionData("currency") as RPKCurrency
            val amount = context.getSessionData("amount") as Int
            if (economyProvider.getBalance(character, currency) + amount <= 1728) {
                economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + amount)
            } else {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-amount-invalid-amount-limit"))
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-add-valid"))
        }

    }

}