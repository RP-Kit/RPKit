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

package com.seventh_root.elysium.chat.bukkit.irc.listener

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.chatchannel.undirected.IRCComponent
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.JoinEvent

/**
 * IRC channel join listener.
 * Prevents unauthorised users joining whitelisted channels.
 */
class IRCChannelJoinListener(private val plugin: ElysiumChatBukkit): ListenerAdapter() {

    override fun onJoin(event: JoinEvent) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class)
        val user = event.user
        if (user != null) {
            ircProvider.addIRCUser(user)
            val verified = user.isVerified
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
            val chatChannel = chatChannelProvider.getChatChannelFromIRCChannel(event.channel.name)
            if (chatChannel != null) {
                if (chatChannel.undirectedPipeline
                        .map { component -> component as? IRCComponent }
                        .filterNotNull()
                        .firstOrNull()
                        ?.isIRCWhitelisted?:false) {
                    if (!verified) {
                        event.getBot<PircBotX>().sendIRC().message(event.channel.name, "/kick " + event.channel.name + " " + user.nick + " Only registered/identified users may join this channel.")
                        event.channel.send().message(user.nick + " attempted to join, but was not registered.")
                    } else if (!(user.channelsVoiceIn.contains(event.channel) || user.channelsHalfOpIn.contains(event.channel) || user.channelsOpIn.contains(event.channel))) {
                        //TODO: Once permissions is part of Elysium, we can make this check permission via account link instead, where available.
                        event.getBot<PircBotX>().sendIRC().message(event.channel.name, "/kick " + event.channel.name + " " + user.nick + " Only authorised users may join this channel.")
                        event.channel.send().message(user.nick + " attempted to join, but was not authorised.")
                    }
                }
            }
        }
    }

}