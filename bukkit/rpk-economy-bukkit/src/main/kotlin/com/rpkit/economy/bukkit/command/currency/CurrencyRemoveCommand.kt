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

package com.rpkit.economy.bukkit.command.currency

import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Currency remove command.
 * Removes a currency.
 */
class CurrencyRemoveCommand(private val plugin: RPKEconomyBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(CurrencyPrompt())
                .withEscapeSequence("cancel")
                .addConversationAbandonedListener { event ->
                    if (!event.gracefulExit()) {
                        val conversable = event.context.forWhom
                        if (conversable is Player) {
                            conversable.sendMessage(plugin.messages["operation-cancelled"])
                        }
                    }
                }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Conversable) return true
        if (!sender.hasPermission("rpkit.economy.command.currency.remove")) {
            sender.sendMessage(plugin.messages["no-permission-currency-remove"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        val currencyService = Services[RPKCurrencyService::class]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages["no-currency-service"])
            return true
        }
        val currencyBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            currencyBuilder.append(args[i]).append(' ')
        }
        currencyBuilder.append(args[args.size - 1])
        val currency = currencyService.getCurrency(currencyBuilder.toString())
        if (currency == null) {
            sender.sendMessage(plugin.messages["currency-remove-invalid-currency"])
            return true
        }
        currencyService.removeCurrency(currency)
        sender.sendMessage(plugin.messages["currency-remove-valid"])
        return true
    }

    private inner class CurrencyPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val currencyService = Services[RPKCurrencyService::class] ?: return false
            val currency = currencyService.getCurrency(input) ?: return false
            context.setSessionData("currencyService", currencyService)
            context.setSessionData("currency", currency)
            return true
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val currencyService = context.getSessionData("currencyService") as RPKCurrencyService
            val currency = context.getSessionData("currency") as RPKCurrency
            currencyService.removeCurrency(currency)
            return CurrencySetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            if (Services[RPKCurrencyService::class] == null) return plugin.messages["no-currency-service"]
            return plugin.messages["currency-remove-invalid-currency"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-remove-prompt"]
        }

    }

    private inner class CurrencySetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-remove-valid"]
        }

    }

}
