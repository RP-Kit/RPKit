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

package com.rpkit.chat.bukkit.speaker

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.database.table.RPKChatChannelSpeakerTable
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile

/**
 * Provides chat channel speaker related operations.
 */
class RPKChatChannelSpeakerService(override val plugin: RPKChatBukkit) : Service {

    /**
     * Gets which channel a Minecraft profile is speaking in.
     * If the Minecraft profile is not currently speaking in a channel, null is returned
     *
     * @param minecraftProfile The Minecraft profile
     * @return The chat channel, or null if the Minecraft profile is not currently speaking
     */
    fun getMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile): RPKChatChannel? {
        return plugin.database.getTable(RPKChatChannelSpeakerTable::class).get(minecraftProfile)?.chatChannel
    }

    /**
     * Sets which channel a Minecraft profile is speaking in.
     *
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel to set
     */
    fun setMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel?) {
        val table = plugin.database.getTable(RPKChatChannelSpeakerTable::class)
        var chatChannelSpeaker = table.get(minecraftProfile)
        if (chatChannelSpeaker == null) {
            if (chatChannel != null) {
                chatChannelSpeaker = RPKChatChannelSpeaker(minecraftProfile = minecraftProfile, chatChannel = chatChannel)
                table.insert(chatChannelSpeaker)
            }
        } else {
            if (chatChannel == null) {
                table.delete(chatChannelSpeaker)
            } else {
                chatChannelSpeaker.chatChannel = chatChannel
                table.update(chatChannelSpeaker)
            }
        }
    }

    /**
     * Stops a Minecraft profile speaking in any channels.
     *
     * @param minecraftProfile The Minecraft profile to stop speaking
     */
    fun removeMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile) {
        val table = plugin.database.getTable(RPKChatChannelSpeakerTable::class)
        val chatChannelSpeaker = table.get(minecraftProfile)
        if (chatChannelSpeaker != null) {
            table.delete(chatChannelSpeaker)
        }
    }

}