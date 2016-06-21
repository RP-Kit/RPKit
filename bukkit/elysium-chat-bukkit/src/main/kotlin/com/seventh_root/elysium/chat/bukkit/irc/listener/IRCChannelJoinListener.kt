package com.seventh_root.elysium.chat.bukkit.irc.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.JoinEvent

class IRCChannelJoinListener(private val plugin: ElysiumChatBukkit): ListenerAdapter() {

    override fun onJoin(event: JoinEvent) {
        val user = event.user
        if (user != null) {
            val verified = user.isVerified
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class.java)
            val chatChannel = chatChannelProvider.getChatChannelFromIRCChannel(event.channel.name)
            if (chatChannel != null) {
                if (chatChannel.isIRCWhitelist) {
                    if (!verified) {
                        event.getBot<PircBotX>().sendIRC().message(event.channel.name, "/kick " + event.channel.name + " " + user.nick + " Only registered/identified users may join this channel.")
                        event.channel.send().message(user.nick + " attempted to join, but was not registered.")
                    } else if (!(user.channelsVoiceIn.contains(event.channel) || user.channelsHalfOpIn.contains(event.channel) || user.channelsOpIn.contains(event.channel))) {
                        event.getBot<PircBotX>().sendIRC().message(event.channel.name, "/kick " + event.channel.name + " " + user.nick + " Only authorised users may join this channel.")
                        event.channel.send().message(user.nick + " attempted to join, but was not authorised.")
                    }
                }
            }
        }
    }

}