/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.chat.bukkit.irc.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.JoinEvent

/**
 * IRC channel join listener.
 * Prevents unauthorised users joining whitelisted channels.
 */
class IRCChannelJoinListener(private val plugin: RPKChatBukkit): ListenerAdapter() {

    override fun onJoin(event: JoinEvent) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
        val user = event.user
        if (user != null) {
            ircProvider.addIRCUser(user)
            val verified = user.isVerified
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
            val chatChannel = chatChannelProvider.getChatChannelFromIRCChannel(event.channel.name)
            if (chatChannel != null) {
                if (chatChannel.undirectedPipeline
                                .mapNotNull { component -> component as? IRCComponent }
                                .firstOrNull()
                                ?.isIRCWhitelisted == true) {
                    if (!verified) {
                        event.getBot<PircBotX>().sendIRC().message(event.channel.name, "/kick " + event.channel.name + " " + user.nick + " Only registered/identified users may join this channel.")
                        event.channel.send().message(user.nick + " attempted to join, but was not registered.")
                    } else if (!(user.channelsVoiceIn.contains(event.channel) || user.channelsHalfOpIn.contains(event.channel) || user.channelsOpIn.contains(event.channel))) {
                        //TODO: Once permissions is part of RPK, we can make this check permission via account link instead, where available.
                        event.getBot<PircBotX>().sendIRC().message(event.channel.name, "/kick " + event.channel.name + " " + user.nick + " Only authorised users may join this channel.")
                        event.channel.send().message(user.nick + " attempted to join, but was not authorised.")
                    }
                }
            }
        }
    }

}