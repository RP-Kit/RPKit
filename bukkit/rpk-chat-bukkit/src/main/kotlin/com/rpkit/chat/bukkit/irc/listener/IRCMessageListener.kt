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

package com.rpkit.chat.bukkit.irc.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

/**
 * IRC message listener.
 * Sends messages to chat channels when received in IRC.
 */
class IRCMessageListener(private val plugin: RPKChatBukkit): ListenerAdapter() {

    override fun onMessage(event: MessageEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val user = event.user
        if (user != null) {
            val sender = playerProvider.getPlayer(user)
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
            val chatChannel = chatChannelProvider.getChatChannelFromIRCChannel(event.channel.name)
            chatChannel?.sendMessage(sender, event.message, chatChannel.directedPipeline, chatChannel.undirectedPipeline.filter { it !is IRCComponent })
        }
    }

}