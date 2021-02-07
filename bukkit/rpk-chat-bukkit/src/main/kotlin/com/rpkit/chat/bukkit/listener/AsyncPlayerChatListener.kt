/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Player chat listener.
 * Cancels normal message processing and passes the message to the appropriate chat channel.
 */
class AsyncPlayerChatListener(private val plugin: RPKChatBukkit) : Listener {

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            val chatChannel = chatChannelService.getMinecraftProfileChannel(minecraftProfile)
            val message = event.message
            var readMessageIndex = 0
            chatChannelService.matchPatterns
                    .map { matchPattern ->
                        val matches = matchPattern.regex.let(::Regex).findAll(message).toList()
                        matches to matchPattern
                    }
                    .flatMap { (matches, matchPattern) -> matches.associateWith { matchPattern }.toList() }
                    .sortedBy { (match, _) -> match.range.first }
                    .forEach { (match, matchPattern) ->
                        sendMessage(
                            chatChannel,
                            message.substring(readMessageIndex, match.range.first),
                            event.player,
                            profile,
                            minecraftProfile,
                            event.isAsynchronous
                        )
                        match.groupValues.forEachIndexed { index, value ->
                            val otherChatChannel = matchPattern.groups[index]
                            if (otherChatChannel != null) {
                                sendMessage(
                                    otherChatChannel,
                                    value,
                                    event.player,
                                    profile,
                                    minecraftProfile,
                                    event.isAsynchronous
                                )
                            }
                        }
                        readMessageIndex = match.range.last + 1
                    }
            if (readMessageIndex < message.length) {
                sendMessage(
                    chatChannel,
                    message.substring(readMessageIndex, message.length),
                    event.player,
                    profile,
                    minecraftProfile,
                    event.isAsynchronous
                )
            }
        } else {
            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
        }
    }

    private fun sendMessage(
        chatChannel: RPKChatChannel?,
        message: String,
        bukkitPlayer: Player,
        profile: RPKThinProfile,
        minecraftProfile: RPKMinecraftProfile,
        isAsynchronous: Boolean
    ) {
        if (chatChannel != null) {
            if (!chatChannel.listeners.any { listenerMinecraftProfile ->
                        listenerMinecraftProfile.id == minecraftProfile.id
                    }) {
                chatChannel.addListener(minecraftProfile, isAsynchronous)
            }
            if (message.isNotBlank()) {
                chatChannel.sendMessage(
                        profile,
                        minecraftProfile,
                        message.trim(),
                        isAsynchronous
                )
            }
        } else {
            bukkitPlayer.sendMessage(plugin.messages["no-chat-channel"])
        }
    }

}
