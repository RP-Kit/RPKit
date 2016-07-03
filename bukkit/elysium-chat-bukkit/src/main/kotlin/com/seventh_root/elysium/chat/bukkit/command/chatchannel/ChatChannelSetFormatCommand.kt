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

package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class ChatChannelSetFormatCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(ChatChannelPrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.chat.command.chatchannel.set.format")) {
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                if (args.size > 0) {
                    val chatChannel = chatChannelProvider.getChatChannel(args[0])
                    if (chatChannel != null) {
                        val formatBuilder = StringBuilder()
                        for (i in 1..args.size - 1 - 1) {
                            formatBuilder.append(args[i]).append(" ")
                        }
                        formatBuilder.append(args[args.size - 1])
                        chatChannel.name = formatBuilder.toString()
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-formatstring-valid")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-set-format")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class ChatChannelPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(input) != null
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-chatchannel-invalid-chatchannel"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("channel", input)
            return ChatChannelFormatStringPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-chatchannel-prompt"))
        }

    }

    private inner class ChatChannelFormatStringPrompt: StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-formatstring-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("format_string", input)
            return ChatChannelFormatStringSetPrompt()
        }

    }

    private inner class ChatChannelFormatStringSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
            val chatChannel = chatChannelProvider.getChatChannel(context.getSessionData("channel") as String)!!
            chatChannel.formatString = context.getSessionData("format_string") as String
            chatChannelProvider.updateChatChannel(chatChannel)
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-formatstring-valid"))
        }

    }

}
