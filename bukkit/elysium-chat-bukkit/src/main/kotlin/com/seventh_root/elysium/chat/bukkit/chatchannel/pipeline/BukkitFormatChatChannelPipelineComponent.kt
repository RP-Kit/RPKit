package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent
import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.FORMATTER
import com.seventh_root.elysium.api.chat.ChatMessageContext
import com.seventh_root.elysium.api.chat.ChatMessagePostProcessContext
import com.seventh_root.elysium.api.chat.exception.ChatChannelMessageFormattingFailureException
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils
import org.bukkit.ChatColor

class BukkitFormatChatChannelPipelineComponent(private val plugin: ElysiumChatBukkit, var formatString: String?) : ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = FORMATTER

    @Throws(ChatChannelMessageFormattingFailureException::class)
    override fun process(message: String, context: ChatMessageContext): String? {
        val characterProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
        val sender = context.sender
        val receiver = context.receiver
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        val receiverCharacter = characterProvider.getActiveCharacter(receiver)
        val chatChannel = context.chatChannel
        var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
        if (formattedMessage.contains("\$message")) {
            formattedMessage = formattedMessage.replace("\$message", message)
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
        val characterProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
        val sender = context.sender
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        val chatChannel = context.chatChannel
        var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
        if (formattedMessage.contains("\$message")) {
            formattedMessage = formattedMessage.replace("\$message", message)
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
        if (formattedMessage.contains("\$receiver-player")) {
            formattedMessage = formattedMessage.replace("\$receiver-player", "\$receiver-player")
        }
        if (formattedMessage.contains("\$receiver-character")) {
            formattedMessage = formattedMessage.replace("\$receiver-character", "\$receiver-character")
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
