package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class ChatChannelJoinCommand(private val plugin: ElysiumChatBukkit) : CommandExecutor {
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
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
            val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
            if (chatChannelProvider.chatChannels.size > 0) {
                val player = playerProvider.getPlayer(sender)
                if (args.size > 0) {
                    val chatChannelBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        chatChannelBuilder.append(args[i]).append(' ')
                    }
                    chatChannelBuilder.append(args[args.size - 1])
                    val chatChannel = chatChannelProvider.getChatChannel(chatChannelBuilder.toString())
                    if (chatChannel != null) {
                        if (sender.hasPermission("elysium.chat.command.chatchannel.join." + chatChannel.name)) {
                            chatChannel.addListener(player)
                            chatChannelProvider.updateChatChannel(chatChannel)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-join-valid")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-join-channel")
                                    .replace("\$channel", chatChannel.name)))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-join-invalid-chatchannel")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channels-available")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class ChatChannelPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java).getChatChannel(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                val channel = chatChannelProvider.getChatChannel(input)!!
                channel.addListener(player)
                chatChannelProvider.updateChatChannel(channel)
            }
            return ChatChannelJoinedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-join-invalid-chatchannel"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
            val channelListBuilder = StringBuilder()
            for (channel in chatChannelProvider.chatChannels) {
                channelListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-list-item")
                        .replace("\$color", ChatColorUtils.closestChatColorToColor(channel.color).toString())
                        .replace("\$name", channel.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-join-prompt")) + "\n" + channelListBuilder.toString()
        }

    }

    private inner class ChatChannelJoinedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-join-valid"))
        }

    }

}
