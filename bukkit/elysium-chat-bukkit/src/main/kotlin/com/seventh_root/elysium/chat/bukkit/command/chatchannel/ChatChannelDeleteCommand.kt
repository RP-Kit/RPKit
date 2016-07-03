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

class ChatChannelDeleteCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
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
        if (sender is Conversable) {
            if (sender.hasPermission("elysium.chat.command.chatchannel.delete")) {
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                if (chatChannelProvider.chatChannels.size > 0) {
                    if (args.size > 0) {
                        val chatChannelBuilder = StringBuilder()
                        for (i in 0..args.size - 1 - 1) {
                            chatChannelBuilder.append(args[i]).append(' ')
                        }
                        chatChannelBuilder.append(args[args.size - 1])
                        val chatChannel = chatChannelProvider.getChatChannel(chatChannelBuilder.toString())
                        if (chatChannel != null) {
                            chatChannelProvider.removeChatChannel(chatChannel)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-delete-valid")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-delete-invalid-chatchannel")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channels-available")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-delete")))
            }
        }
        return true
    }

    private inner class ChatChannelPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                val channel = chatChannelProvider.getChatChannel(input)!!
                chatChannelProvider.removeChatChannel(channel)
            }
            return ChatChannelDeletedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-delete-invalid-chatchannel"))
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-delete-prompt"))
        }

    }

    private inner class ChatChannelDeletedPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-delete-valid"))
        }

    }

}
