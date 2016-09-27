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

package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.undirected.IRCComponent
import com.seventh_root.elysium.chat.bukkit.speaker.ElysiumChatChannelSpeakerProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import java.awt.Color

class ElysiumChatChannelProviderImpl(private val plugin: ElysiumChatBukkit): ElysiumChatChannelProvider {

    override val chatChannels: MutableList<ElysiumChatChannel> = plugin.config.getConfigurationSection("chat-channels")
                .getKeys(false)
                .mapIndexed { id, channelName -> ElysiumChatChannelImpl(
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

    override fun getChatChannel(id: Int): ElysiumChatChannel? {
        return chatChannels[id]
    }

    override fun getChatChannel(name: String): ElysiumChatChannel? {
        return chatChannels.filter { it.name == name }.firstOrNull()
    }

    override fun addChatChannel(chatChannel: ElysiumChatChannel) {
        chatChannels.add(chatChannel)
    }

    override fun removeChatChannel(chatChannel: ElysiumChatChannel) {
        chatChannels.remove(chatChannel)
    }

    override fun updateChatChannel(chatChannel: ElysiumChatChannel) {

    }

    override fun getPlayerChannel(player: ElysiumPlayer): ElysiumChatChannel? {
        return plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelSpeakerProvider::class).getPlayerChannel(player)
    }

    override fun setPlayerChannel(player: ElysiumPlayer, channel: ElysiumChatChannel) {
        val oldChannel = getPlayerChannel(player)
        if (oldChannel != null) {
            oldChannel.removeSpeaker(player)
            updateChatChannel(oldChannel)
        }
        channel.addSpeaker(player)
        updateChatChannel(channel)
    }

    override fun getChatChannelFromIRCChannel(ircChannel: String): ElysiumChatChannel? {
        return chatChannels.filter { chatChannel ->
            chatChannel.undirectedPipeline
                .map { component -> component as? IRCComponent }
                .filterNotNull()
                .firstOrNull() != null
        }
        .firstOrNull()
    }

}
