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
 * Payment set interval command.
 * Sets the interval at which the payment group charges.
 */
class PaymentSetIntervalCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {
    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(IntervalPrompt())
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
        if (sender.hasPermission("rpkit.payments.command.payment.set.interval")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val player = playerProvider.getPlayer(sender)
                    val character = characterProvider.getActiveCharacter(player)
                    val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                    val paymentGroup = paymentGroupProvider.getPaymentGroup(args.joinToString(" "))
                    if (paymentGroup != null) {
                        if (paymentGroup.owners.contains(character)) {
                            val conversation = conversationFactory.buildConversation(sender)
                            conversation.context.setSessionData("payment_group", paymentGroup)
                            conversation.begin()
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-invalid-owner")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-invalid-group")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-payment-set-interval")))
        }
        return true
    }

    private inner class IntervalPrompt: NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-prompt"))
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toLong() > 0
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-invalid-validation"))
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-invalid-number"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
            val paymentGroup = context.getSessionData("payment_group") as RPKPaymentGroup
            paymentGroup.interval = input.toLong() * 1000L
            paymentGroupProvider.updatePaymentGroup(paymentGroup)
            return IntervalSetPrompt()
        }

    }

    private inner class IntervalSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-set-interval-valid"))
        }

    }

}