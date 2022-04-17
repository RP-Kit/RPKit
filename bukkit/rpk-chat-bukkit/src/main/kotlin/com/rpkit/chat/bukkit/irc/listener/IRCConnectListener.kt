/*
 * Copyright 2022 Ren Binden
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

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.service.Services
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent

/**
 * IRC connect listener.
 * Joins each of the channels upon connecting.
 */
class IRCConnectListener : ListenerAdapter() {

    override fun onConnect(event: ConnectEvent?) {
        val ircService = Services[RPKIRCService::class.java] ?: return
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return
        for (channel in chatChannelService.chatChannels) {
            val ircChannel = channel.undirectedPipeline
                .firstNotNullOfOrNull { component -> component as? IRCComponent }
                ?.ircChannel
            if (ircChannel != null) {
                ircService.joinChannel(ircChannel)
            }
        }
    }

}