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

package com.seventh_root.elysium.chat.bukkit.mute

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumChatChannelMuteTable
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Provides chat channel mute related services.
 */
class ElysiumChatChannelMuteProvider(private val plugin: ElysiumChatBukkit): ServiceProvider {

    /**
     * Adds a chat channel mute.
     *
     * @param player The player
     * @param chatChannel The chat channel
     */
    fun addChatChannelMute(player: ElysiumPlayer, chatChannel: ElysiumChatChannel) {
        if (!hasPlayerMutedChatChannel(player, chatChannel)) {
            plugin.core.database.getTable(ElysiumChatChannelMuteTable::class).insert(
                    ElysiumChatChannelMute(
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
    fun removeChatChannelMute(player: ElysiumPlayer, chatChannel: ElysiumChatChannel) {
        val chatChannelMuteTable = plugin.core.database.getTable(ElysiumChatChannelMuteTable::class)
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
    fun hasPlayerMutedChatChannel(player: ElysiumPlayer, chatChannel: ElysiumChatChannel): Boolean {
        return plugin.core.database.getTable(ElysiumChatChannelMuteTable::class).get(player, chatChannel) != null
    }

}