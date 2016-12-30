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

package com.rpkit.chat.bukkit.speaker

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.database.table.RPKChatChannelSpeakerTable
import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer

/**
 * Provides chat channel speaker related operations.
 */
class RPKChatChannelSpeakerProvider(private val plugin: RPKChatBukkit): ServiceProvider {

    /**
     * Gets which channel a player is speaking in.
     * If the player is not currently speaking in a channel, null is returned
     *
     * @param player The player
     * @return The chat channel, or null if the player is not currently speaking
     */
    fun getPlayerChannel(player: RPKPlayer): RPKChatChannel? {
        return plugin.core.database.getTable(RPKChatChannelSpeakerTable::class).get(player)?.chatChannel
    }

    /**
     * Sets the channel a player is speaking in.
     *
     * @param player The player
     * @param chatChannel The chat channel to set
     */
    fun setPlayerChannel(player: RPKPlayer, chatChannel: RPKChatChannel) {
        val table = plugin.core.database.getTable(RPKChatChannelSpeakerTable::class)
        var chatChannelSpeaker = table.get(player)
        if (chatChannelSpeaker == null) {
            chatChannelSpeaker = RPKChatChannelSpeaker(player = player, chatChannel = chatChannel)
            table.insert(chatChannelSpeaker)
        } else {
            chatChannelSpeaker.chatChannel = chatChannel
            table.update(chatChannelSpeaker)
        }
    }

    /**
     * Stops a player speaking in any channels.
     *
     * @param player The player to stop speaking
     */
    fun removePlayerChannel(player: RPKPlayer) {
        val table = plugin.core.database.getTable(RPKChatChannelSpeakerTable::class)
        val chatChannelSpeaker = table.get(player)
        if (chatChannelSpeaker != null) {
            table.delete(chatChannelSpeaker)
        }
    }

}