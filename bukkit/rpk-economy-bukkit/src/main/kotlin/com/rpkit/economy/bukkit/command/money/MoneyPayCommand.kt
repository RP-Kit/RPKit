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
 * Money pay command.
 * Pays money to another character from the user's active character.
 */
class MoneyPayCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {
    
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
            if (sender.hasPermission("rpkit.economy.command.money.pay")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                val fromBukkitPlayer = sender
                val fromPlayer = playerProvider.getPlayer(fromBukkitPlayer)
                val fromCharacter = characterProvider.getActiveCharacter(fromPlayer)
                if (fromCharacter != null) {
                    if (args.size > 0) {
                        val toBukkitPlayer = plugin.server.getPlayer(args[0])
                        if (toBukkitPlayer != null) {
                            val toPlayer = playerProvider.getPlayer(toBukkitPlayer)
                            val toCharacter = characterProvider.getActiveCharacter(toPlayer)
                            if (toCharacter != null) {
                                if (args.size > 1) {
                                    try {
                                        val amount = args[1].toInt()
                                        if (amount >= 0) {
                                            if (args.size > 2) {
                                                var currency = currencyProvider.getCurrency(args[2])
                                                if (currency == null) {
                                                    currency = currencyProvider.defaultCurrency
                                                }
                                                if (currency != null) {
                                                    if (fromBukkitPlayer.location.distanceSquared(toBukkitPlayer.location) <= plugin.config.getDouble("payments.maximum-distance") * plugin.config.getDouble("payments.maximum-distance")) {
                                                        if (economyProvider.getBalance(fromCharacter, currency) >= amount) {
                                                            if (economyProvider.getBalance(toCharacter, currency) + amount <= 1728) {
                                                                economyProvider.transfer(fromCharacter, toCharacter, currency, amount)
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-valid")))
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-valid"))
                                                                        .replace("\$amount", amount.toString())
                                                                        .replace("\$currency", if (amount == 1) currency.nameSingular else currency.namePlural)
                                                                        .replace("\$character", toCharacter.name)
                                                                        .replace("\$player", toPlayer.name))
                                                                toCharacter.player?.bukkitPlayer?.player?.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-received"))
                                                                        .replace("\$amount", amount.toString())
                                                                        .replace("\$currency", if (amount == 1) currency.nameSingular else currency.namePlural)
                                                                        .replace("\$character", fromCharacter.name)
                                                                        .replace("\$player", fromPlayer.name))
                                                            } else {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-limit")))
                                                            }
                                                        } else {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-balance")))
                                                        }
                                                    } else {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-invalid-player-distance")))
                                                    }
                                                } else {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-currency-invalid-currency")))
                                                }
                                            } else {
                                                conversationFactory.buildConversation(sender).begin()
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-negative")))
                                        }
                                    } catch (exception: NumberFormatException) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-number")))
                                    }
                                } else {
                                    conversationFactory.buildConversation(sender).begin()
                                }
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-character-invalid-character")))
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-invalid-player-offline")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-money-pay")))
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
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-invalid-player-offline"))
        }

    }

    private inner class PlayerSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-valid"))
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
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-currency-prompt")) + "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).currencies
                            .map { currency -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-currency-prompt-list-item")).replace("\$currency", currency.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-currency-invalid-currency"))
        }
    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-currency-valid"))
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
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-negative"))
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-number"))
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneyPayCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-valid"))
        }

    }

    private inner class MoneyPayCompletePrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val fromBukkitPlayer = context.forWhom as Player
            val fromPlayer = playerProvider.getPlayer(fromBukkitPlayer)
            val fromCharacter = characterProvider.getActiveCharacter(fromPlayer)
            val toPlayer = context.getSessionData("player") as RPKPlayer
            val toCharacter = characterProvider.getActiveCharacter(toPlayer)
            val currency = context.getSessionData("currency") as RPKCurrency
            val amount = context.getSessionData("amount") as Int
            if (fromCharacter != null) {
                if (toCharacter != null) {
                    val toBukkitPlayer = toPlayer.bukkitPlayer
                    if (toBukkitPlayer != null) {
                        if (fromBukkitPlayer.location.distanceSquared(toBukkitPlayer.player.location) <= plugin.config.getDouble("payments.maximum-distance") * plugin.config.getDouble("payments.maximum-distance")) {
                            if (economyProvider.getBalance(fromCharacter, currency) >= amount) {
                                if (economyProvider.getBalance(toCharacter, currency) + amount <= 1728) {
                                    economyProvider.transfer(fromCharacter, toCharacter, currency, amount)
                                    toCharacter.player?.bukkitPlayer?.player?.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-received"))
                                            .replace("\$amount", amount.toString())
                                            .replace("\$currency", if (amount == 1) currency.nameSingular else currency.namePlural)
                                            .replace("\$character", fromCharacter.name)
                                            .replace("\$player", fromPlayer.name))
                                    return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-valid"))
                                            .replace("\$amount", amount.toString())
                                            .replace("\$currency", if (amount == 1) currency.nameSingular else currency.namePlural)
                                            .replace("\$character", toCharacter.name)
                                            .replace("\$player", toPlayer.name)
                                } else {
                                    return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-limit"))
                                }
                            } else {
                                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-amount-invalid-amount-balance"))
                            }
                        } else {
                            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-invalid-player-distance"))
                        }
                    } else {
                        return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-pay-player-invalid-player-distance"))
                    }
                } else {
                    return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.recipient-no-character"))
                }
            } else {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character"))
            }
        }

    }
}