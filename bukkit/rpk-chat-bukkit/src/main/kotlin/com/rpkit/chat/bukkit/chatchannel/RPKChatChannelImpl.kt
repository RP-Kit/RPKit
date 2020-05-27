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

package com.rpkit.chat.bukkit.chatchannel

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.context.DirectedChatChannelMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.context.UndirectedChatChannelMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.context.UndirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelMessageEvent
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteProvider
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKThinProfile
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
    override val speakerMinecraftProfiles: List<RPKMinecraftProfile>
        get() = plugin.server.onlinePlayers
                .mapNotNull { player -> plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(player) }
                .filter { minecraftProfile -> plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).getMinecraftProfileChannel(minecraftProfile) == this }
    override val listeners: List<RPKPlayer>
        get() = plugin.server.onlinePlayers
                .filter { player -> player.hasPermission("rpkit.chat.listen.$name") }
                .map { player -> plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(player) }
                .filter { player -> !plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).hasPlayerMutedChatChannel(player, this) }
    override val listenerMinecraftProfiles: List<RPKMinecraftProfile>
        get() = plugin.server.onlinePlayers
                .filter { player -> player.hasPermission("rpkit.chat.listen.$name") }
                .mapNotNull { player -> plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(player) }
                .filter { minecraftProfile -> !plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).hasMinecraftProfileMutedChatChannel(minecraftProfile, this) }

    override fun addSpeaker(speaker: RPKPlayer) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).setPlayerChannel(speaker, this)
    }

    override fun addSpeaker(speaker: RPKMinecraftProfile) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class).setMinecraftProfileChannel(speaker, this)
    }

    override fun removeSpeaker(speaker: RPKPlayer) {
        val chatChannelSpeakerProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class)
        if (chatChannelSpeakerProvider.getPlayerChannel(speaker) == this) {
            chatChannelSpeakerProvider.removePlayerChannel(speaker)
        }
    }

    override fun removeSpeaker(speaker: RPKMinecraftProfile) {
        val chatChannelSpeakerProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelSpeakerProvider::class)
        if (chatChannelSpeakerProvider.getMinecraftProfileChannel(speaker) == this) {
            chatChannelSpeakerProvider.removeMinecraftProfileChannel(speaker)
        }
    }

    override fun addListener(listener: RPKPlayer) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).removeChatChannelMute(listener, this)
    }

    override fun addListener(listener: RPKMinecraftProfile, isAsync: Boolean) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).removeChatChannelMute(listener, this, isAsync)
    }

    override fun removeListener(listener: RPKPlayer) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).addChatChannelMute(listener, this)
    }

    override fun removeListener(listener: RPKMinecraftProfile) {
        plugin.core.serviceManager.getServiceProvider(RPKChatChannelMuteProvider::class).addChatChannelMute(listener, this)
    }

    override fun sendMessage(sender: RPKPlayer, message: String, isAsync: Boolean) {
        val bukkitPlayer = sender.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                val profile = minecraftProfile.profile
                sendMessage(profile, minecraftProfile, message, isAsync)
            }
        }
    }

    override fun sendMessage(sender: RPKThinProfile, senderMinecraftProfile: RPKMinecraftProfile?, message: String, isAsync: Boolean) {
        sendMessage(sender, senderMinecraftProfile, message, directedPipeline, undirectedPipeline, isAsync)
    }

    override fun sendMessage(sender: RPKPlayer, message: String, directedPipeline: List<DirectedChatChannelPipelineComponent>, undirectedPipeline: List<UndirectedChatChannelPipelineComponent>, isAsync: Boolean) {
        val bukkitPlayer = sender.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                val profile = minecraftProfile.profile
                sendMessage(profile, minecraftProfile, message, directedPipeline, undirectedPipeline, isAsync)
            }
        }
    }

    override fun sendMessage(sender: RPKThinProfile, senderMinecraftProfile: RPKMinecraftProfile?, message: String, directedPipeline: List<DirectedChatChannelPipelineComponent>, undirectedPipeline: List<UndirectedChatChannelPipelineComponent>, isAsync: Boolean) {
        val event = RPKBukkitChatChannelMessageEvent(sender, senderMinecraftProfile, this, message, isAsync)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        listenerMinecraftProfiles.forEach { listener ->
            var context: DirectedChatChannelMessageContext = DirectedChatChannelMessageContextImpl(
                    event.chatChannel,
                    event.profile,
                    event.minecraftProfile,
                    listener,
                    event.message
            )
            directedPipeline.forEach { component ->
                context = component.process(context)
            }
        }
        var context: UndirectedChatChannelMessageContext = UndirectedChatChannelMessageContextImpl(
                event.chatChannel,
                event.profile,
                event.minecraftProfile,
                event.message
        )
        undirectedPipeline.forEach { component ->
            context = component.process(context)
        }
    }

}