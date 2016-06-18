package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible

class ChatChannelSpeakCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
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
            if (sender.hasPermission("elysium.chat.command.chatchannel.speak")) {
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java)
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
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
                            if (sender.hasPermission("elysium.chat.command.chatchannel.speak." + chatChannel.name)) {
                                val oldChannel = chatChannelProvider.getPlayerChannel(player)
                                if (oldChannel != null) {
                                    oldChannel.removeSpeaker(player)
                                    chatChannelProvider.updateChatChannel(oldChannel)
                                }
                                chatChannel.addSpeaker(player)
                                chatChannel.addListener(player)
                                chatChannelProvider.updateChatChannel(chatChannel)
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-speak-valid").replace("\$channel", chatChannel.name)))
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-speak-channel"))
                                        .replace("\$channel", chatChannel.name))
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-speak-invalid-chatchannel")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-chat-channels-available")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-speak")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class ChatChannelPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java).getChatChannel(input) != null && (context.forWhom as Permissible).hasPermission("elysium.chat.command.chatchannel.speak." + input)
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                val channel = chatChannelProvider.getChatChannel(input)!!
                val oldChannel = chatChannelProvider.getPlayerChannel(player)
                if (oldChannel != null) {
                    oldChannel.removeSpeaker(player)
                    chatChannelProvider.updateChatChannel(oldChannel)
                }
                channel.addSpeaker(player)
                channel.addListener(player)
                chatChannelProvider.updateChatChannel(channel)
                context.setSessionData("channel", channel)
            }
            return ChatChannelSpeakingPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            if (plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java).getChatChannel(invalidInput) == null) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-speak-invalid-chatchannel"))
            } else if (!(context.forWhom as Permissible).hasPermission("elysium.chat.command.chatchannel.speak." + invalidInput)) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-speak-channel")
                        .replace("\$channel", invalidInput))
            }
            return ""
        }

        override fun getPromptText(context: ConversationContext): String {
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java)
            val channelListBuilder = StringBuilder()
            for (channel in chatChannelProvider.chatChannels) {
                channelListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-list-item")
                        .replace("\$color", ChatColorUtils.closestChatColorToColor(channel.color).toString())
                        .replace("\$name", channel.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-speak-prompt") + "\n" + channelListBuilder.toString())
        }

    }

    private inner class ChatChannelSpeakingPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&',
                    plugin.config.getString("messages.chatchannel-speak-valid")
                            .replace("\$channel", (context.getSessionData("channel") as ElysiumChatChannel).name))
        }

    }

}
