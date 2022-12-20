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

package com.rpkit.chat.bukkit.listener

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (event.player.hasPlayedBefore()) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return
        val muteService = Services[RPKChatChannelMuteService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player) ?: return
        chatChannelService.chatChannels.forEach { chatChannel ->
            if (!chatChannel.isJoinedByDefault) {
                muteService.addChatChannelMute(minecraftProfile, chatChannel)
            }
        }
        val defaultChatChannel = chatChannelService.defaultChatChannel
        if (defaultChatChannel != null) {
            chatChannelService.setMinecraftProfileChannel(minecraftProfile, defaultChatChannel)
        }
    }
}