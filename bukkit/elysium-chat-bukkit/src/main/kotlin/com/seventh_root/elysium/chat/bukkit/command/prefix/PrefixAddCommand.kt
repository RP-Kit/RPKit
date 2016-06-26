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
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixImpl
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player


class PrefixAddCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(NamePrompt())
                .withEscapeSequence("cancel")
                .addConversationAbandonedListener { event ->
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
            if (sender.hasPermission("elysium.chat.command.prefix.add")) {
                if (args.size > 1) {
                    val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
                    if (prefixProvider.getPrefix(args[0]) == null) {
                        prefixProvider.addPrefix(ElysiumPrefixImpl(name = args[0], prefix = ChatColor.translateAlternateColorCodes('&', args[1])))
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-add-valid")))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-set-name-invalid-name")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-prefix-add")))
            }
        }
        return true
    }

    private inner class NamePrompt: ValidatingPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-set-name-prompt"))
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
            return prefixProvider.getPrefix(input) == null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("name", input)
            return NameSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-set-name-invalid-name"))
        }

    }

    private inner class NameSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return PrefixPrompt()
        }

        override fun getPromptText(context: ConversationContext): String? {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-set-name-valid"))
        }

    }

    private inner class PrefixPrompt: StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-set-prefix-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("prefix", ChatColor.translateAlternateColorCodes('&', input))
            return PrefixSetPrompt()
        }

    }

    private inner class PrefixSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return PrefixAddedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-set-prefix-valid"))
        }

    }

    private inner class PrefixAddedPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val prefix = ElysiumPrefixImpl(
                    name = context.getSessionData("name") as String,
                    prefix = context.getSessionData("prefix") as String
            )
            plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class).addPrefix(prefix)
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.prefix-add-valid"))
        }

    }
}