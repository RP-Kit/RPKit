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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider

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
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("addChatChannelMute(minecraftProfile, chatChannel)"))
    fun addChatChannelMute(player: RPKPlayer, chatChannel: RPKChatChannel) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                addChatChannelMute(minecraftProfile, chatChannel)
            }
        }
    }

    /**
     * Adds a chat channel mute.
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     */
    fun addChatChannelMute(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel) {
        if (!hasMinecraftProfileMutedChatChannel(minecraftProfile, chatChannel)) {
            plugin.core.database.getTable(RPKChatChannelMuteTable::class).insert(
                    RPKChatChannelMute(
                            minecraftProfile = minecraftProfile,
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
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("removeChatChannelMute(minecraftProfile, chatChannel)"))
    fun removeChatChannelMute(player: RPKPlayer, chatChannel: RPKChatChannel) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                removeChatChannelMute(minecraftProfile, chatChannel)
            }
        }
    }

    /**
     * Removes a chat channel mute.
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     */
    fun removeChatChannelMute(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel) {
        val chatChannelMuteTable = plugin.core.database.getTable(RPKChatChannelMuteTable::class)
        val chatChannelMute = chatChannelMuteTable.get(minecraftProfile, chatChannel)
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
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("hasMinecraftProfileMutedChatChannel(minecraftProfile, chatChannel)"))
    fun hasPlayerMutedChatChannel(player: RPKPlayer, chatChannel: RPKChatChannel): Boolean {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return hasMinecraftProfileMutedChatChannel(minecraftProfile, chatChannel)
            }
        }
        return false
    }

    /**
     * Checks whether a Minecraft profile has muted a chat channel.
     *
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     * @return Whether the Minecraft profile has muted the chat channel
     */
    fun hasMinecraftProfileMutedChatChannel(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel): Boolean {
        return plugin.core.database.getTable(RPKChatChannelMuteTable::class).get(minecraftProfile, chatChannel) != null
    }

}