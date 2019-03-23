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

package com.rpkit.chat.bukkit.chatchannel

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelCreateEvent
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelDeleteEvent
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelSwitchEvent
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelUpdateEvent
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import java.awt.Color

/**
 * Chat channel provider implementation.
 */
class RPKChatChannelProviderImpl(private val plugin: RPKChatBukkit): RPKChatChannelProvider {

    override val chatChannels: MutableList<RPKChatChannel> = plugin.config.getConfigurationSection("chat-channels")
                .getKeys(false)
                .mapIndexed { id, channelName -> RPKChatChannelImpl(
                        plugin = plugin,
                        id = id,
                        name = channelName,
                        color = Color(
                                plugin.config.getInt("chat-channels.$channelName.color.red"),
                                plugin.config.getInt("chat-channels.$channelName.color.green"),
                                plugin.config.getInt("chat-channels.$channelName.color.blue")
                        ),
                        radius = plugin.config.getDouble("chat-channels.$channelName.radius"),
                        directedPipeline = plugin.config.getList("chat-channels.$channelName.directed-pipeline") as List<DirectedChatChannelPipelineComponent>,
                        undirectedPipeline = plugin.config.getList("chat-channels.$channelName.undirected-pipeline") as List<UndirectedChatChannelPipelineComponent>,
                        matchPattern = plugin.config.getString("chat-channels.$channelName.match-pattern"),
                        isJoinedByDefault = plugin.config.getBoolean("chat-channels.$channelName.joined-by-default")
                ) }
                .toMutableList()

    override fun getChatChannel(id: Int): RPKChatChannel? {
        return chatChannels[id]
    }

    override fun getChatChannel(name: String): RPKChatChannel? {
        return chatChannels.filter { it.name == name }.firstOrNull()
    }

    override fun addChatChannel(chatChannel: RPKChatChannel) {
        val event = RPKBukkitChatChannelCreateEvent(chatChannel)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        chatChannels.add(event.chatChannel)
    }

    override fun removeChatChannel(chatChannel: RPKChatChannel) {
        val event = RPKBukkitChatChannelDeleteEvent(chatChannel)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        chatChannels.remove(event.chatChannel)
    }

    override fun updateChatChannel(chatChannel: RPKChatChannel) {
        val event = RPKBukkitChatChannelUpdateEvent(chatChannel)
        plugin.server.pluginManager.callEvent(event)
    }

    override fun getPlayerChannel(player: RPKPlayer): RPKChatChannel? {
        return plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).getPlayerChannel(player)
    }

    override fun setPlayerChannel(player: RPKPlayer, channel: RPKChatChannel) {
        val oldChannel = getPlayerChannel(player)
        if (oldChannel != null) {
            oldChannel.removeSpeaker(player)
            updateChatChannel(oldChannel)
        }
        channel.addSpeaker(player)
        updateChatChannel(channel)
    }

    override fun getMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile): RPKChatChannel? {
        return plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).getMinecraftProfileChannel(minecraftProfile)
    }

    override fun setMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile, channel: RPKChatChannel?) {
        var oldChannel = getMinecraftProfileChannel(minecraftProfile)
        val event = RPKBukkitChatChannelSwitchEvent(minecraftProfile, oldChannel, channel)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        oldChannel = event.oldChannel
        if (oldChannel != null) {
            oldChannel.removeSpeaker(minecraftProfile)
            updateChatChannel(oldChannel)
        }
        val chatChannel = event.chatChannel
        if (chatChannel != null) {
            chatChannel.addSpeaker(minecraftProfile)
            updateChatChannel(chatChannel)
        }
    }

    override fun getChatChannelFromIRCChannel(ircChannel: String): RPKChatChannel? {
        return chatChannels.filter { chatChannel ->
            chatChannel.undirectedPipeline
                .map { component -> component as? IRCComponent }
                .filterNotNull()
                .firstOrNull() != null
        }
        .firstOrNull()
    }

}
