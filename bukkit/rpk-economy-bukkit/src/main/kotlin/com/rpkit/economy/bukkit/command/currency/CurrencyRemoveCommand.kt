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
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Currency remove command.
 * Removes a currency.
 */
class CurrencyRemoveCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {
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
        if (sender is Conversable) {
            if (sender.hasPermission("rpkit.economy.command.currency.remove")) {
                if (args.isNotEmpty()) {
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                    val currencyBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        currencyBuilder.append(args[i]).append(' ')
                    }
                    currencyBuilder.append(args[args.size - 1])
                    val currency = currencyProvider.getCurrency(currencyBuilder.toString())
                    if (currency != null) {
                        currencyProvider.removeCurrency(currency)
                        sender.sendMessage(plugin.messages["currency-remove-valid"])
                    } else {
                        sender.sendMessage(plugin.messages["currency-remove-invalid-currency"])
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-currency-remove"])
            }
        }
        return true
    }

    private inner class CurrencyPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            currencyProvider.removeCurrency(currencyProvider.getCurrency(input)!!)
            return CurrencySetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["currency-remove-invalid-currency"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-remove-prompt"]
        }

    }

    private inner class CurrencySetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["currency-remove-valid"]
        }

    }

}
