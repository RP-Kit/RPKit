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

package com.rpkit.economy.bukkit.command.currency

import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyImpl
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Currency add command.
 * Adds a currency.
 */
class CurrencyAddCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {
    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(NamePrompt())
            .withEscapeSequence("cancel")
            .addConversationAbandonedListener { event ->
        if (!event.gracefulExit()) {
            val conversable = event.context.forWhom
            if (conversable is Player) {
                conversable.sendMessage(plugin.messages["operation-cancelled"])
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Conversable) {
            if (sender.hasPermission("rpkit.economy.command.currency.add")) {
                conversationFactory.buildConversation(sender).begin()
            } else {
                sender.sendMessage(plugin.messages["no-permission-currency-add"])
            }
        }
        return true
    }

    private inner class NamePrompt: ValidatingPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-name-prompt"]
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            return currencyProvider.getCurrency(input) == null
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["currency-set-name-invalid-name"]
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("name", input)
            return NameSetPrompt()
        }

    }

    private inner class NameSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return NameSingularPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-name-valid"]
        }

    }

    private inner class NameSingularPrompt: StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-name-singular-prompt"]
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt {
            context.setSessionData("name_singular", input)
            return NameSingularSetPrompt()
        }

    }

    private inner class NameSingularSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return NamePluralPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-name-singular-valid"]
        }

    }

    private inner class NamePluralPrompt: StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-name-plural-prompt"]
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt {
            context.setSessionData("name_plural", input)
            return NamePluralSetPrompt()
        }

    }

    private inner class NamePluralSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return RatePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-name-plural-valid"]
        }

    }

    private inner class RatePrompt: NumericPrompt() {
        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("rate", input.toDouble())
            return RateSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-rate-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toDouble() > 0
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String? {
            return plugin.messages["currency-set-rate-invalid-rate-negative"]
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String? {
            return plugin.messages["currency-set-rate-invalid-rate-number"]
        }
    }

    private inner class RateSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return DefaultAmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-rate-valid"]
        }

    }

    private inner class DefaultAmountPrompt: NumericPrompt() {
        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("default_amount", input.toInt())
            return DefaultAmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-default-amount-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["currency-set-default-amount-invalid-negative"]
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String? {
            return plugin.messages["currency-set-default-amount-invalid-number"]
        }
    }

    private inner class DefaultAmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return MaterialPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-default-amount-valid"]
        }

    }

    private inner class MaterialPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return Material.matchMaterial(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("material", Material.matchMaterial(input))
            return MaterialSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-material-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["currency-set-material-invalid-material"]
        }

    }

    private inner class MaterialSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyAddedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-set-material-valid"]
        }

    }

    private inner class CurrencyAddedPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            currencyProvider.addCurrency(
                    RPKCurrencyImpl(
                            name = context.getSessionData("name") as String,
                            nameSingular = context.getSessionData("name_singular") as String,
                            namePlural = context.getSessionData("name_plural") as String,
                            rate = context.getSessionData("rate") as Double,
                            defaultAmount = context.getSessionData("default_amount") as Int,
                            material = context.getSessionData("material") as Material
                    )
            )
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-add-valid"]
        }

    }

}
