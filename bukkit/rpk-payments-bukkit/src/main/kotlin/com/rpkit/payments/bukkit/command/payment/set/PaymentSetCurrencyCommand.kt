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
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
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

/**
 * Payment set currency command.
 * Sets the currency a payment group charges in.
 */
class PaymentSetCurrencyCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(CurrencyPrompt())
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
        if (!sender.hasPermission("rpkit.payments.command.payment.set.currency")) {
            sender.sendMessage(plugin.messages["no-permission-payment-set-currency"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["payment-set-currency-usage"])
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
        val paymentGroup = paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.joinToString(" ")))
        if (paymentGroup == null) {
            sender.sendMessage(plugin.messages["payment-set-currency-invalid-group"])
            return true
        }
        paymentGroup.owners.thenAccept { owners ->
            if (!owners.contains(character)) {
                sender.sendMessage(plugin.messages["payment-set-currency-invalid-owner"])
                return@thenAccept
            }
            val conversation = conversationFactory.buildConversation(sender)
            conversation.context.setSessionData("payment_group", paymentGroup)
            conversation.begin()
        }
        return true
    }

    private inner class CurrencyPrompt : ValidatingPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            val currencyService = Services[RPKCurrencyService::class.java] ?: return plugin.messages["no-currency-service"]
            return plugin.messages["payment-set-currency-prompt"] + "\n" +
                    plugin.messages["currency-list-title"] + "\n" +
                    currencyService
                            .currencies
                            .joinToString("\n") { currency -> plugin.messages["currency-list-item", mapOf("currency" to currency.name.value)] }
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            if (Services[RPKPaymentGroupService::class.java] == null) return false
            return Services[RPKCurrencyService::class.java]?.getCurrency(RPKCurrencyName(input)) != null
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            if (Services[RPKCurrencyService::class.java] == null) return plugin.messages["no-currency-service"]
            return plugin.messages["payment-set-currency-invalid-currency"]
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val paymentGroupService = Services[RPKPaymentGroupService::class.java] ?: return END_OF_CONVERSATION
            val currencyService = Services[RPKCurrencyService::class.java] ?: return END_OF_CONVERSATION
            val paymentGroup = context.getSessionData("payment_group") as RPKPaymentGroup
            paymentGroup.currency = currencyService.getCurrency(RPKCurrencyName(input))
            paymentGroupService.updatePaymentGroup(paymentGroup)
            return CurrencySetPrompt()
        }

    }

    private inner class CurrencySetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["payment-set-currency-valid"]
        }

    }

}