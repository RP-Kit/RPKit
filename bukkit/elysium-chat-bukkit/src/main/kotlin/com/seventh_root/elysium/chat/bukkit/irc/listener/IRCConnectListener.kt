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
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent

/**
 * IRC connect listener.
 * Joins each of the channels upon connecting.
 */
class IRCConnectListener(private val plugin: ElysiumChatBukkit): ListenerAdapter() {

    override fun onConnect(event: ConnectEvent?) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class)
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
        for (channel in chatChannelProvider.chatChannels) {
            val ircChannel = channel.undirectedPipeline
                    .map { component -> component as? IRCComponent }
                    .filterNotNull()
                    .firstOrNull()?.ircChannel
            if (ircChannel != null) {
                ircProvider.ircBot.sendIRC().joinChannel(ircChannel)
            }
        }
    }

}