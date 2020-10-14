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

package com.rpkit.chat.bukkit.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

/**
 * Player chat listener.
 * Cancels normal message processing and passes the message to the appropriate chat channel.
 */
class AsyncPlayerChatListener(private val plugin: RPKChatBukkit) : Listener {

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        val chatChannelService = Services[RPKChatChannelService::class] ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            var chatChannel = chatChannelService.getMinecraftProfileChannel(minecraftProfile)
            var message = event.message
            for (otherChannel in chatChannelService.chatChannels) {
                val matchPattern = otherChannel.matchPattern
                if (matchPattern != null) {
                    if (matchPattern.isNotEmpty()) {
                        if (message.matches(matchPattern.toRegex())) {
                            chatChannel = otherChannel
                            val pattern = Pattern.compile(matchPattern)
                            val matcher = pattern.matcher(message)
                            if (matcher.matches()) {
                                if (matcher.groupCount() > 0) {
                                    message = matcher.group(1)
                                }
                            }
                            if (!chatChannel.listenerMinecraftProfiles.any { listenerMinecraftProfile ->
                                        listenerMinecraftProfile.id == minecraftProfile.id
                                    }) {
                                chatChannel.addListener(minecraftProfile, event.isAsynchronous)
                                chatChannelService.updateChatChannel(chatChannel, event.isAsynchronous)
                            }
                        }
                    }
                }
            }
            if (chatChannel != null) {
                chatChannel.sendMessage(profile, minecraftProfile, message, event.isAsynchronous)
            } else {
                event.player.sendMessage(plugin.messages["no-chat-channel"])
            }
        } else {
            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
        }
    }

}
