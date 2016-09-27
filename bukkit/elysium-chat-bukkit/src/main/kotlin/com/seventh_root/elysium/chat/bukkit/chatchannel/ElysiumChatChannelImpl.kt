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
import com.seventh_root.elysium.chat.bukkit.chatchannel.context.DirectedChatChannelMessageContextImpl
import com.seventh_root.elysium.chat.bukkit.chatchannel.context.UndirectedChatChannelMessageContextImpl
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.DirectedChatChannelMessageContext
import com.seventh_root.elysium.chat.bukkit.context.UndirectedChatChannelMessageContext
import com.seventh_root.elysium.chat.bukkit.mute.ElysiumChatChannelMuteProvider
import com.seventh_root.elysium.chat.bukkit.speaker.ElysiumChatChannelSpeakerProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import java.awt.Color


class ElysiumChatChannelImpl(
        private val plugin: ElysiumChatBukkit,
        override var id: Int = 0,
        override val name: String,
        override val color: Color,
        override val radius: Double,
        override val directedPipeline: List<DirectedChatChannelPipelineComponent>,
        override val undirectedPipeline: List<UndirectedChatChannelPipelineComponent>,
        override val matchPattern: String?,
        override var isJoinedByDefault: Boolean
) : ElysiumChatChannel {


    override val speakers: List<ElysiumPlayer>
        get() = plugin.server.onlinePlayers
                .map { player -> plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(player) }
                .filter { player -> plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelSpeakerProvider::class).getPlayerChannel(player) == this }
    override val listeners: List<ElysiumPlayer>
        get() = plugin.server.onlinePlayers
                .filter { player -> player.hasPermission("elysium.chat.listen.$name") }
                .map { player -> plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(player) }
                .filter { player -> !plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelMuteProvider::class).hasPlayerMutedChatChannel(player, this) }

    override fun addSpeaker(speaker: ElysiumPlayer) {
        plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelSpeakerProvider::class).setPlayerChannel(speaker, this)
    }

    override fun removeSpeaker(speaker: ElysiumPlayer) {
        if (plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelSpeakerProvider::class).getPlayerChannel(speaker) == this) {
            plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelSpeakerProvider::class).removePlayerChannel(speaker)
        }
    }

    override fun addListener(listener: ElysiumPlayer) {
        plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelMuteProvider::class).removeChatChannelMute(listener, this)
    }

    override fun removeListener(listener: ElysiumPlayer) {
        plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelMuteProvider::class).addChatChannelMute(listener, this)
    }

    override fun sendMessage(sender: ElysiumPlayer, message: String) {
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