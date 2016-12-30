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

package com.rpkit.payments.bukkit.command.payment.set

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Payment set currency command.
 * Sets the currency a payment group charges in.
 */
class PaymentSetCurrencyCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(CurrencyPrompt())
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
        if (sender.hasPermission("rpkit.payments.command.payment.set.currency")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val player = playerProvider.getPlayer(sender)
                    val character = characterProvider.getActiveCharacter(player)
                    val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                    val paymentGroup = paymentGroupProvider.getPaymentGroup(args[0])
                    if (paymentGroup != null) {
                        if (paymentGroup.owners.contains(character)) {
                            val conversation = conversationFactory.buildConversation(sender)
                            conversation.context.setSessionData("payment_group", paymentGroup)
                            conversation.begin()
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-currency-invalid-owner")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-currency-invalid-group")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-currency-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-payment-set-currency")))
        }
        return true
    }

    private inner class CurrencyPrompt: ValidatingPrompt() {

        override fun getPromptText(context: ConversationContext?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-currency-prompt"))
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input) != null
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-currency-invalid-currency"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val paymentGroup = context.getSessionData("payment_group") as RPKPaymentGroup
            paymentGroup.currency = currencyProvider.getCurrency(input)
            paymentGroupProvider.updatePaymentGroup(paymentGroup)
            return CurrencySetPrompt()
        }

    }

    private inner class CurrencySetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext?): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-currency-valid"))
        }

    }

}