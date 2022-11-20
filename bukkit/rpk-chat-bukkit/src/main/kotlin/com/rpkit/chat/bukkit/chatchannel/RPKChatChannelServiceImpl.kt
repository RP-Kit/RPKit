/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.chat.bukkit.chatchannel.format.FormatPart
import com.rpkit.chat.bukkit.chatchannel.matchpattern.RPKChatChannelMatchPattern
import com.rpkit.chat.bukkit.chatchannel.matchpattern.RPKChatChannelMatchPatternImpl
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPostFormatPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.DiscordComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.chat.bukkit.discord.DiscordChannel
import com.rpkit.chat.bukkit.event.chatchannel.RPKBukkitChatChannelSwitchEvent
import com.rpkit.chat.bukkit.irc.IRCChannel
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.awt.Color
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Chat channel service implementation.
 */
class RPKChatChannelServiceImpl(override val plugin: RPKChatBukkit) : RPKChatChannelService {

    override val chatChannels: MutableList<RPKChatChannel> = plugin.config.getConfigurationSection("chat-channels")
            ?.getKeys(false)
            ?.map { channelName ->
                RPKChatChannelImpl(
                        plugin = plugin,
                        name = RPKChatChannelName(channelName),
                        color = Color(
                                plugin.config.getInt("chat-channels.$channelName.color.red"),
                                plugin.config.getInt("chat-channels.$channelName.color.green"),
                                plugin.config.getInt("chat-channels.$channelName.color.blue")
                        ),
                        radius = plugin.config.getDouble("chat-channels.$channelName.radius"),
                        directedPreFormatPipeline = plugin.config.getList("chat-channels.$channelName.directed-pre-format-pipeline") as List<DirectedPreFormatPipelineComponent>,
                        format = plugin.config.getList("chat-channels.$channelName.format") as List<FormatPart>,
                        directedPostFormatPipeline = plugin.config.getList("chat-channels.$channelName.directed-post-format-pipeline") as List<DirectedPostFormatPipelineComponent>,
                        undirectedPipeline = plugin.config.getList("chat-channels.$channelName.undirected-pipeline") as List<UndirectedPipelineComponent>,
                        isJoinedByDefault = plugin.config.getBoolean("chat-channels.$channelName.joined-by-default")
                )
            }
            ?.toMutableList()
            ?: mutableListOf()

    override val defaultChatChannel: RPKChatChannel? = plugin.config.getString("default-chat-channel")
        ?.let(::RPKChatChannelName)
        ?.let(::getChatChannel)

    override val matchPatterns: List<RPKChatChannelMatchPattern> = plugin.config.getConfigurationSection("match-patterns")
            ?.getKeys(false)
            ?.map { pattern ->
                RPKChatChannelMatchPatternImpl(
                        regex = pattern,
                        groups = plugin.config.getConfigurationSection("match-patterns.$pattern.groups")
                                ?.getKeys(false)
                                ?.map { group -> group.toInt() to plugin.config.getString("match-patterns.$pattern.groups.$group")?.let { getChatChannel(RPKChatChannelName(it)) } }
                                ?.filter { (_, chatChannel) -> chatChannel != null }
                                ?.filterIsInstance<Pair<Int, RPKChatChannel>>()
                                ?.toMap()
                                ?: emptyMap()
                )
            }
            ?: emptyList()

    override fun getChatChannel(name: RPKChatChannelName): RPKChatChannel? {
        return chatChannels.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }
    }

    override fun getMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKChatChannel?> {
        val speakerService = Services[RPKChatChannelSpeakerService::class.java] ?: return CompletableFuture.completedFuture(null)
        return speakerService.getMinecraftProfileChannel(minecraftProfile)
    }

    override fun setMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile, channel: RPKChatChannel?): CompletableFuture<Void> {
        return getMinecraftProfileChannel(minecraftProfile).thenAcceptAsync { oldChannel ->
            val event = RPKBukkitChatChannelSwitchEvent(minecraftProfile, oldChannel, channel, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@thenAcceptAsync
            oldChannel?.removeSpeaker(minecraftProfile)?.join()
            val chatChannel = event.chatChannel
            chatChannel?.addSpeaker(minecraftProfile)?.join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set minecraft profile channel", exception)
            throw exception
        }
    }

    override fun getChatChannelFromIRCChannel(ircChannel: IRCChannel): RPKChatChannel? {
        return chatChannels.firstOrNull { chatChannel ->
            chatChannel.undirectedPipeline.any { component -> component is IRCComponent && component.ircChannel == ircChannel }
        }
    }

    override fun getChatChannelFromDiscordChannel(discordChannel: DiscordChannel): RPKChatChannel? {
        return chatChannels.firstOrNull { chatChannel ->
            chatChannel.undirectedPipeline.any { component -> component is DiscordComponent && component.discordChannel == discordChannel }
        }
    }

}
