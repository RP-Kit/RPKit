package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class ChatChannelSetIRCChannelCommand(private val plugin: ElysiumChatBukkit) : CommandExecutor {
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
            if (sender.hasPermission("elysium.chat.command.chatchannel.set.ircchannel")) {
                conversationFactory.buildConversation(sender).begin()
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-set-ircchannel")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class ChatChannelPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core!!.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java).getChatChannel(input) != null
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-chatchannel-invalid-chatchannel"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("channel", input)
            return ChatChannelIRCChannelPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-chatchannel-prompt"))
        }

    }

    private inner class ChatChannelIRCChannelPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return input.matches("/([#&][^\\x07\\x2C\\s]{0,200})/".toRegex())
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-channel-invalid-irc-channel"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("irc_channel", input)
            return ChatChannelIRCChannelSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-channel-prompt"))
        }

    }

    private inner class ChatChannelIRCChannelSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val chatChannelProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
            val chatChannel = chatChannelProvider.getChatChannel(context.getSessionData("channel") as String)!!
            chatChannel.ircChannel = context.getSessionData("irc_channel") as String
            chatChannelProvider.updateChatChannel(chatChannel)
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-channel-valid"))
        }

    }

}
