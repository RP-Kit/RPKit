/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Payment set amount command.
 * Sets the amount a payment group charges.
 */
class PaymentSetAmountCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(AmountPrompt())
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
        if (sender.hasPermission("rpkit.payments.command.payment.set.amount")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                    if (minecraftProfile != null) {
                        val character = characterProvider.getActiveCharacter(minecraftProfile)
                        val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                        val paymentGroup = paymentGroupProvider.getPaymentGroup(args.joinToString(" "))
                        if (paymentGroup != null) {
                            if (paymentGroup.owners.contains(character)) {
                                val conversation = conversationFactory.buildConversation(sender)
                                conversation.context.setSessionData("payment_group", paymentGroup)
                                conversation.begin()
                            } else {
                                sender.sendMessage(plugin.messages["payment-set-amount-invalid-owner"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["payment-set-amount-invalid-group"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["no-minecraft-profile"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["payment-set-amount-usage"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-payment-set-amount"])
        }
        return true
    }

    private inner class AmountPrompt: NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["payment-set-amount-prompt"]
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["payment-set-amount-invalid-number"]
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
            val paymentGroup = context.getSessionData("payment_group") as RPKPaymentGroup
            paymentGroup.amount = input.toInt()
            paymentGroupProvider.updatePaymentGroup(paymentGroup)
            return AmountSetPrompt()
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["payment-set-amount-valid"]
        }

    }

}