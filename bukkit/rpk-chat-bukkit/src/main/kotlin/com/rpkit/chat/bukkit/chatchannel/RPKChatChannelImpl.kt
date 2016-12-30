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
import com.rpkit.chat.bukkit.chatchannel.context.DirectedChatChannelMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.context.UndirectedChatChannelMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.context.UndirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteProvider
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import java.awt.Color

/**
 * Chat channel implementation.
 */
class RPKChatChannelImpl(
        private val plugin: RPKChatBukkit,
        override var id: Int = 0,
        override val name: String,
        override val color: Color,
        override val radius: Double,
        override val directedPipeline: List<DirectedChatChannelPipelineComponent>,
        override val undirectedPipeline: List<UndirectedChatChannelPipelineComponent>,
        override val matchPattern: String?,
        override var isJoinedByDefault: Boolean
) : RPKChatChannel {


    override val speakers: List<RPKPlayer>
        get() = plugin.server.onlinePlayers
                .map { player -> plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(player) }
                .filter { player -> plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).getPlayerChannel(player) == this }
    override val listeners: List<RPKPlayer>
        get() = plugin.server.onlinePlayers
                .filter { player -> player.hasPermission("rpkit.chat.listen.$name") }
                .map { player -> plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(player) }
                .filter { player -> !plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).hasPlayerMutedChatChannel(player, this) }

    override fun addSpeaker(speaker: RPKPlayer) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).setPlayerChannel(speaker, this)
    }

    override fun removeSpeaker(speaker: RPKPlayer) {
        if (plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).getPlayerChannel(speaker) == this) {
            plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).removePlayerChannel(speaker)
        }
    }

    override fun addListener(listener: RPKPlayer) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).removeChatChannelMute(listener, this)
    }

    override fun removeListener(listener: RPKPlayer) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).addChatChannelMute(listener, this)
    }

    override fun sendMessage(sender: RPKPlayer, message: String) {
        listeners.forEach { listener ->
            var context: DirectedChatChannelMessageContext = DirectedChatChannelMessageContextImpl(this, sender, listener, message)
            directedPipeline.forEach { component ->
                context = component.process(context)
            }
        }
        var context: UndirectedChatChannelMessageContext = UndirectedChatChannelMessageContextImpl(this, sender, message)
        undirectedPipeline.forEach { component ->
            context = component.process(context)
        }
    }

}