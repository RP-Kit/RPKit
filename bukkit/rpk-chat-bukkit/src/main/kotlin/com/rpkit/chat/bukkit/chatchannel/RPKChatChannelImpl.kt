/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.chat.bukkit.chatchannel.context.DirectedPostFormatMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.context.DirectedPreFormatMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.context.UndirectedMessageContextImpl
import com.rpkit.chat.bukkit.chatchannel.format.FormatPart
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPostFormatPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPostFormatMessageContext
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import com.rpkit.chat.bukkit.context.UndirectedMessageContext
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelMessageEvent
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteService
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Chat channel implementation.
 */
class RPKChatChannelImpl(
        private val plugin: RPKChatBukkit,
        override val name: RPKChatChannelName,
        override val color: Color,
        override val radius: Double,
        override val directedPreFormatPipeline: List<DirectedPreFormatPipelineComponent>,
        override val format: List<FormatPart>,
        override val directedPostFormatPipeline: List<DirectedPostFormatPipelineComponent>,
        override val undirectedPipeline: List<UndirectedPipelineComponent>,
        override val isJoinedByDefault: Boolean
) : RPKChatChannel {

    override val speakers: CompletableFuture<List<RPKMinecraftProfile>>
        get() = plugin.server.onlinePlayers
            .mapNotNull { player -> Services[RPKMinecraftProfileService::class.java]?.getPreloadedMinecraftProfile(player) }
            .map { minecraftProfile -> minecraftProfile to Services[RPKChatChannelSpeakerService::class.java]?.getMinecraftProfileChannel(minecraftProfile) }
            .fold(listOf<CompletableFuture<RPKMinecraftProfile?>>()) { futures, (minecraftProfile, channelFuture) ->
                futures + (channelFuture?.thenApply { channel -> if (channel == this) minecraftProfile else null } ?: CompletableFuture.completedFuture(null))
            }
            .let { futures ->
                CompletableFuture.allOf(*futures.toTypedArray())
                    .thenApplyAsync {
                        futures.mapNotNull(CompletableFuture<RPKMinecraftProfile?>::join)
                    }
            }

    override val listeners: CompletableFuture<List<RPKMinecraftProfile>>
        get() = plugin.server.onlinePlayers
            .filter { player -> player.hasPermission("rpkit.chat.listen.${name.value}") }
            .mapNotNull { player -> Services[RPKMinecraftProfileService::class.java]?.getPreloadedMinecraftProfile(player) }
            .map { minecraftProfile -> minecraftProfile to Services[RPKChatChannelMuteService::class.java]?.hasMinecraftProfileMutedChatChannel(minecraftProfile, this) }
            .fold(listOf<CompletableFuture<RPKMinecraftProfile?>>()) { futures, (minecraftProfile, mutedFuture) ->
                futures + (mutedFuture?.thenApply { muted -> if (!muted) minecraftProfile else null } ?: CompletableFuture.completedFuture(null))
            }
            .let { futures ->
                CompletableFuture.allOf(*futures.toTypedArray())
                    .thenApplyAsync {
                        futures.mapNotNull(CompletableFuture<RPKMinecraftProfile?>::join)
                    }
            }

    override fun addSpeaker(speaker: RPKMinecraftProfile): CompletableFuture<Void> {
        val speakerService = Services[RPKChatChannelSpeakerService::class.java] ?: return CompletableFuture.completedFuture(null)
        return speakerService.setMinecraftProfileChannel(speaker, this)
    }

    override fun removeSpeaker(speaker: RPKMinecraftProfile): CompletableFuture<Void> {
        val speakerService = Services[RPKChatChannelSpeakerService::class.java] ?: return CompletableFuture.completedFuture(null)
        return speakerService.getMinecraftProfileChannel(speaker).thenAcceptAsync { chatChannel ->
            if (chatChannel == this) {
                speakerService.removeMinecraftProfileChannel(speaker).join()
            }
        }
    }

    override fun addListener(listener: RPKMinecraftProfile): CompletableFuture<Void> {
        val muteService = Services[RPKChatChannelMuteService::class.java] ?: return CompletableFuture.completedFuture(null)
        return muteService.removeChatChannelMute(listener, this)
    }

    override fun removeListener(listener: RPKMinecraftProfile): CompletableFuture<Void> {
        val muteService = Services[RPKChatChannelMuteService::class.java] ?: return CompletableFuture.completedFuture(null)
        return muteService.addChatChannelMute(listener, this)
    }

    override fun sendMessage(
            sender: RPKThinProfile,
            senderMinecraftProfile: RPKMinecraftProfile?,
            message: String,
            isAsync: Boolean,
            callback: RPKChatChannelMessageCallback?
    ) {
        sendMessage(
            sender,
            senderMinecraftProfile,
            message,
            directedPreFormatPipeline,
            format,
            directedPostFormatPipeline,
            undirectedPipeline,
            isAsync,
            callback
        )
    }

    override fun sendMessage(
            sender: RPKThinProfile,
            senderMinecraftProfile: RPKMinecraftProfile?,
            message: String,
            directedPreFormatPipeline: List<DirectedPreFormatPipelineComponent>,
            format: List<FormatPart>,
            directedPostFormatPipeline: List<DirectedPostFormatPipelineComponent>,
            undirectedPipeline: List<UndirectedPipelineComponent>,
            isAsync: Boolean,
            callback: RPKChatChannelMessageCallback?
    ) {
        val event = RPKBukkitChatChannelMessageEvent(sender, senderMinecraftProfile, this, message, isAsync)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.server.scheduler.runTask(plugin, Runnable {
            listeners.thenAcceptAsync { listeners ->
                listeners.forEach { listener ->
                    var preFormatContext: DirectedPreFormatMessageContext = DirectedPreFormatMessageContextImpl(
                        event.chatChannel,
                        event.profile,
                        event.minecraftProfile,
                        listener,
                        event.message
                    )
                    directedPreFormatPipeline.forEach { component ->
                        preFormatContext = component.process(preFormatContext).join()
                    }
                    var postFormatContext: DirectedPostFormatMessageContext = DirectedPostFormatMessageContextImpl(
                        preFormatContext.chatChannel,
                        preFormatContext.senderProfile,
                        preFormatContext.senderMinecraftProfile,
                        listener,
                        format.flatMap { part -> part.toChatComponents(preFormatContext).join().toList() }.toTypedArray(),
                        preFormatContext.isCancelled
                    )
                    directedPostFormatPipeline.forEach { component ->
                        postFormatContext = component.process(postFormatContext).join()
                    }
                }
                var context: UndirectedMessageContext = UndirectedMessageContextImpl(
                    event.chatChannel,
                    event.profile,
                    event.minecraftProfile,
                    event.message
                )
                undirectedPipeline.forEach { component ->
                    context = component.process(context).join()
                }
                callback?.invoke()
            }
        })
    }

}