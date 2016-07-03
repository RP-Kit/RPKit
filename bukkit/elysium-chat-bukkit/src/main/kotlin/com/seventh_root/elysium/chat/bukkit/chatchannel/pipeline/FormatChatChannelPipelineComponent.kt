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

package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent.Type.FORMATTER
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.chat.bukkit.exception.ChatChannelMessageFormattingFailureException
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils
import org.bukkit.ChatColor

class FormatChatChannelPipelineComponent(private val plugin: ElysiumChatBukkit, var formatString: String?): ChatChannelPipelineComponent {

    override val type: ChatChannelPipelineComponent.Type
        get() = FORMATTER

    override fun process(message: String, context: ChatMessageContext): String? {
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
        val sender = context.sender
        val receiver = context.receiver
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        val receiverCharacter = characterProvider.getActiveCharacter(receiver)
        val chatChannel = context.chatChannel
        var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
        if (formattedMessage.contains("\$message")) {
            formattedMessage = formattedMessage.replace("\$message", message)
        }
        if (formattedMessage.contains("\$sender-prefix")) {
            formattedMessage = formattedMessage.replace("\$sender-prefix", prefixProvider.getPrefix(sender))
        }
        if (formattedMessage.contains("\$sender-player")) {
            formattedMessage = formattedMessage.replace("\$sender-player", sender.name)
        }
        if (formattedMessage.contains("\$sender-character")) {
            if (senderCharacter != null) {
                formattedMessage = formattedMessage.replace("\$sender-character", senderCharacter.name)
            } else {
                throw ChatChannelMessageFormattingFailureException(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
            }
        }
        if (formattedMessage.contains("\$receiver-prefix")) {
            formattedMessage = formattedMessage.replace("\$receiver-prefix", prefixProvider.getPrefix(receiver))
        }
        if (formattedMessage.contains("\$receiver-player")) {
            formattedMessage = formattedMessage.replace("\$receiver-player", receiver.name)
        }
        if (formattedMessage.contains("\$receiver-character")) {
            if (receiverCharacter != null) {
                formattedMessage = formattedMessage.replace("\$receiver-character", receiverCharacter.name)
            } else {
                return null
            }
        }
        if (formattedMessage.contains("\$channel")) {
            formattedMessage = formattedMessage.replace("\$channel", chatChannel.name)
        }
        if (formattedMessage.contains("\$color") || formattedMessage.contains("\$colour")) {
            val chatColorString = ChatColorUtils.closestChatColorToColor(chatChannel.color).toString()
            formattedMessage = formattedMessage.replace("\$color", chatColorString).replace("\$colour", chatColorString)
        }
        return formattedMessage
    }

    override fun postProcess(message: String, context: ChatMessagePostProcessContext): String? {
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
        val sender = context.sender
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        val chatChannel = context.chatChannel
        var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
        if (formattedMessage.contains("\$message")) {
            formattedMessage = formattedMessage.replace("\$message", message)
        }
        if (formattedMessage.contains("\$sender-prefix")) {
            formattedMessage = formattedMessage.replace("\$sender-prefix", prefixProvider.getPrefix(sender))
        }
        if (formattedMessage.contains("\$sender-player")) {
            formattedMessage = formattedMessage.replace("\$sender-player", sender.name)
        }
        if (formattedMessage.contains("\$sender-character")) {
            if (senderCharacter != null) {
                formattedMessage = formattedMessage.replace("\$sender-character", senderCharacter.name)
            } else {
                throw ChatChannelMessageFormattingFailureException(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
            }
        }
        if (formattedMessage.contains("\$channel")) {
            formattedMessage = formattedMessage.replace("\$channel", chatChannel.name)
        }
        if (formattedMessage.contains("\$color") || formattedMessage.contains("\$colour")) {
            val chatColorString = ChatColorUtils.closestChatColorToColor(chatChannel.color).toString()
            formattedMessage = formattedMessage.replace("\$color", chatColorString).replace("\$colour", chatColorString)
        }
        return formattedMessage
    }

}
