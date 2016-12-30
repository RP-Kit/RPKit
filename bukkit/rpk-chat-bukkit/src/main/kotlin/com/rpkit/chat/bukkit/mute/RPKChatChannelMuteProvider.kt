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

package com.rpkit.chat.bukkit.mute

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.database.table.RPKChatChannelMuteTable
import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer

/**
 * Provides chat channel mute related services.
 */
class RPKChatChannelMuteProvider(private val plugin: RPKChatBukkit): ServiceProvider {

    /**
     * Adds a chat channel mute.
     *
     * @param player The player
     * @param chatChannel The chat channel
     */
    fun addChatChannelMute(player: RPKPlayer, chatChannel: RPKChatChannel) {
        if (!hasPlayerMutedChatChannel(player, chatChannel)) {
            plugin.core.database.getTable(RPKChatChannelMuteTable::class).insert(
                    RPKChatChannelMute(
                            player = player,
                            chatChannel = chatChannel
                    )
            )
        }
    }

    /**
     * Removes a chat channel mute.
     *
     * @param player The player
     * @param chatChannel The chat channel
     */
    fun removeChatChannelMute(player: RPKPlayer, chatChannel: RPKChatChannel) {
        val chatChannelMuteTable = plugin.core.database.getTable(RPKChatChannelMuteTable::class)
        val chatChannelMute = chatChannelMuteTable.get(player, chatChannel)
        if (chatChannelMute != null) {
            chatChannelMuteTable.delete(chatChannelMute)
        }
    }

    /**
     * Checks whether a player has muted a chat channel.
     *
     * @param player The player
     * @param chatChannel The chat channel
     * @return Whether the player has muted the chat channel
     */
    fun hasPlayerMutedChatChannel(player: RPKPlayer, chatChannel: RPKChatChannel): Boolean {
        return plugin.core.database.getTable(RPKChatChannelMuteTable::class).get(player, chatChannel) != null
    }

}