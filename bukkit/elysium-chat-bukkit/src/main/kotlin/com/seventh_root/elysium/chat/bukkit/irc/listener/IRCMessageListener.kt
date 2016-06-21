package com.seventh_root.elysium.chat.bukkit.irc.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent


class IRCMessageListener(private val plugin: ElysiumChatBukkit): ListenerAdapter() {

    override fun onMessage(event: MessageEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
        val user = event.user
        if (user != null) {
            val sender = playerProvider.getPlayer(user)
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java)
            val chatChannel = chatChannelProvider.getChatChannelFromIRCChannel(event.channel.name)
            chatChannel?.listeners
                    ?.filter { listener -> listener.bukkitPlayer != null }
                    ?.filter { listener -> listener.bukkitPlayer?.player?.isOnline ?: false }
                    ?.forEach { listener ->
                        listener.bukkitPlayer?.player?.sendMessage(chatChannel.processMessage(event.message, ChatMessageContext(chatChannel, sender, listener)))
                    }
            chatChannel?.postProcess(event.message, ChatMessagePostProcessContext(chatChannel, sender))
        }
    }

}