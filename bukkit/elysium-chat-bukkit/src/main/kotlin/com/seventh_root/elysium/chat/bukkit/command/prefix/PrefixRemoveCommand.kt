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

package com.seventh_root.elysium.chat.bukkit.command.prefix

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player


class PrefixRemoveCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(PrefixPrompt()).withEscapeSequence("cancel").addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Conversable) {
            if (sender.hasPermission("elysium.chat.command.prefix.remove")) {
                if (args.size > 0) {
                    val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
                    val prefixBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        prefixBuilder.append(args[i]).append(' ')
                    }
                    prefixBuilder.append(args[args.size - 1])
                    val prefix = prefixProvider.getPrefix(prefixBuilder.toString())
                    if (prefix != null) {
                        prefixProvider.removePrefix(prefix)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-remove-valid")))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-remove-invalid-prefix")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-prefix-remove")))
            }
        }
        return true
    }

    private inner class PrefixPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class).getPrefix(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
            prefixProvider.removePrefix(prefixProvider.getPrefix(input)!!)
            return PrefixSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-remove-invalid-prefix"))
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-remove-prompt"))
        }

    }

    private inner class PrefixSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-remove-valid"))
        }

    }
    
}