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

package com.rpkit.chat.bukkit.mute

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.database.table.RPKChatChannelMuteTable
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelMuteEvent
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelUnmuteEvent
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile

/**
 * Provides chat channel mute related services.
 */
class RPKChatChannelMuteService(override val plugin: RPKChatBukkit) : Service {

    /**
     * Adds a chat channel mute.
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     */
    fun addChatChannelMute(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel) {
        if (!hasMinecraftProfileMutedChatChannel(minecraftProfile, chatChannel)) {
            val event = RPKBukkitChatChannelMuteEvent(minecraftProfile, chatChannel)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.database.getTable(RPKChatChannelMuteTable::class).insert(
                    RPKChatChannelMute(
                            minecraftProfile = event.minecraftProfile,
                            chatChannel = event.chatChannel
                    )
            )
        }
    }

    /**
     * Removes a chat channel mute.
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     */
    fun removeChatChannelMute(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel, isAsync: Boolean = false) {
        val event = RPKBukkitChatChannelUnmuteEvent(minecraftProfile, chatChannel, isAsync)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val chatChannelMuteTable = plugin.database.getTable(RPKChatChannelMuteTable::class)
        val chatChannelMute = chatChannelMuteTable.get(event.minecraftProfile, event.chatChannel)
        if (chatChannelMute != null) {
            chatChannelMuteTable.delete(chatChannelMute)
        }
    }

    /**
     * Checks whether a Minecraft profile has muted a chat channel.
     *
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     * @return Whether the Minecraft profile has muted the chat channel
     */
    fun hasMinecraftProfileMutedChatChannel(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel): Boolean {
        return plugin.database.getTable(RPKChatChannelMuteTable::class).get(minecraftProfile, chatChannel) != null
    }

}