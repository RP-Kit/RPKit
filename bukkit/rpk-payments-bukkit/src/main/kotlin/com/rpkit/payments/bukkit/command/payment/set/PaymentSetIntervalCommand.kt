/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupName
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

/**
 * Payment set interval command.
 * Sets the interval at which the payment group charges.
 */
class PaymentSetIntervalCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(IntervalPrompt())
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
        if (!sender.hasPermission("rpkit.payments.command.payment.set.interval")) {
            sender.sendMessage(plugin.messages["no-permission-payment-set-interval"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["payment-set-interval-usage"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages["no-payment-group-service"])
            return true
        }
        paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.joinToString(" "))).thenAccept getPaymentGroup@{ paymentGroup ->
            if (paymentGroup == null) {
                sender.sendMessage(plugin.messages["payment-set-interval-invalid-group"])
                return@getPaymentGroup
            }
            paymentGroup.owners.thenAccept { owners ->
                if (!owners.contains(character)) {
                    sender.sendMessage(plugin.messages["payment-set-interval-invalid-owner"])
                    return@thenAccept
                }
                val conversation = conversationFactory.buildConversation(sender)
                conversation.context.setSessionData("payment_group", paymentGroup)
                conversation.begin()
            }
        }
        return true
    }

    private inner class IntervalPrompt : NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            if (Services[RPKPaymentGroupService::class.java] == null) return plugin.messages["no-payment-group-service"]
            return plugin.messages["payment-set-interval-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toLong() > 0
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["payment-set-interval-invalid-validation"]
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["payment-set-interval-invalid-number"]
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            val paymentGroupService = Services[RPKPaymentGroupService::class.java] ?: return END_OF_CONVERSATION
            val paymentGroup = context.getSessionData("payment_group") as RPKPaymentGroup
            paymentGroup.interval = Duration.of(input.toLong(), SECONDS)
            paymentGroupService.updatePaymentGroup(paymentGroup)
            return IntervalSetPrompt()
        }

    }

    private inner class IntervalSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["payment-set-interval-valid"]
        }

    }

}