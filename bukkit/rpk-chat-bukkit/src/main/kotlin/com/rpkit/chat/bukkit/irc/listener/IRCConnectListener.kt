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
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent

/**
 * IRC connect listener.
 * Joins each of the channels upon connecting.
 */
class IRCConnectListener(private val plugin: RPKChatBukkit): ListenerAdapter() {

    override fun onConnect(event: ConnectEvent?) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
        for (channel in chatChannelProvider.chatChannels) {
            val ircChannel = channel.undirectedPipeline
                    .mapNotNull { component -> component as? IRCComponent }
                    .firstOrNull()?.ircChannel
            if (ircChannel != null) {
                ircProvider.ircBot.sendIRC().joinChannel(ircChannel)
            }
        }
    }

}