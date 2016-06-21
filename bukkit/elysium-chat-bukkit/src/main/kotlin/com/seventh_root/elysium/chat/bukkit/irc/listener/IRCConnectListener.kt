package com.seventh_root.elysium.chat.bukkit.irc.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent


class IRCConnectListener(private val plugin: ElysiumChatBukkit): ListenerAdapter() {

    override fun onConnect(event: ConnectEvent?) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class.java)
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java)
        for (channel in chatChannelProvider.chatChannels) {
            if (channel.isIRCEnabled) {
                ircProvider.ircBot.sendIRC().joinChannel(channel.ircChannel)
            }
        }
    }

}