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

import com.rpkit.chat.bukkit.chatchannel.format.FormatPart
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPostFormatPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Represents a chat channel
 */
interface RPKChatChannel {

    /**
     * The name of the chat channel.
     */
    val name: RPKChatChannelName

    /**
     * The colour used to represent the chat channel.
     * Associating a colour with each channel allows players to easily distinguish between channels.
     */
    val color: Color

    /**
     * The radius up to which messages sent to the chat channel may be heard.
     * If the radius is less than or equal to zero, chat messages are global.
     * This is measured in metres, which is the size of one Minecraft block.
     */
    val radius: Double

    /**
     * A list of all speakers in the channel.
     * If a speaker sends a message without indicating who it is directed to, it will be sent to this channel.
     * Chat participants may only be speakers in a single channel.
     */
    val speakers: CompletableFuture<List<RPKMinecraftProfile>>

    /**
     * A list of all listeners in the channel.
     * If a message is sent to a channel, it will be heard by all listeners.
     * Chat participants may listen to multiple channels.
     */
    val listeners: CompletableFuture<List<RPKMinecraftProfile>>

    /**
     * The directed pre-format pipeline for the channel.
     * Messages to this channel will pass through this pipeline for each listener, which will apply garble, languages,
     * drunken slurs, radius filters, etc.
     * They will then be passed through the formatter which transforms the message into chat components, and then
     * finally the post-format pipeline, which performs operations such as sending the message or showing it to
     * players with snoop enabled.
     */
    val directedPreFormatPipeline: List<DirectedPreFormatPipelineComponent>

    /**
     * The format to transform the message with after passing it through the pre-format pipeline and before passing it
     * to the post-format pipeline.
     */
    val format: List<FormatPart>

    /**
     * The directed post-format pipeline for the channel.
     * Messages to this channel will first pass through the pre-format pipeline (which applies garble, languages,
     * drunken slurs, radius filters etc), then the formatter (which transforms the message to chat components),
     * and then finally this pipeline (which sends the message or shows it to players with snoop enabled), for each
     * player.
     */
    val directedPostFormatPipeline: List<DirectedPostFormatPipelineComponent>

    /**
     * The undirected pipeline for the channel.
     * Messages to this channel will pass through this pipeline once, so that messages may be logged or sent to IRC only
     * a single time.
     */
    val undirectedPipeline: List<UndirectedPipelineComponent>

    /**
     * Whether this channel should be joined by default.
     * If a channel is joined by default, all new players are added as listeners to the channel upon joining for the
     * first time. If they are not, the channel is muted until they join it.
     */
    val isJoinedByDefault: Boolean

    /**
     * Adds a speaker to the channel.
     *
     * @param speaker The chat participant to add
     */
    fun addSpeaker(speaker: RPKMinecraftProfile): CompletableFuture<Void>

    /**
     * Removes a speaker from the channel.
     *
     * @param speaker The chat participant to remove
     */
    fun removeSpeaker(speaker: RPKMinecraftProfile): CompletableFuture<Void>

    /**
     * Adds a listener to the channel.
     *
     * @param listener The Minecraft profile to add
     * @param isAsync Whether adding the listener is being done asynchronously
     */
    fun addListener(listener: RPKMinecraftProfile, isAsync: Boolean = false): CompletableFuture<Void>

    /**
     * Removes a listener from the channel.
     *
     * @param listener The Minecraft profile to remove
     */
    fun removeListener(listener: RPKMinecraftProfile): CompletableFuture<Void>

    /**
     * Sends a message to the channel, passing it through the directed pipeline once for each listener, and the
     * undirected pipeline once.
     *
     * @param sender The profile sending the message
     * @param senderMinecraftProfile The Minecraft profile used to send the message, or null if not sent from Minecraft
     * @param message The message
     * @param isAsync Whether the message is being sent asynchronously
     */
    fun sendMessage(
            sender: RPKThinProfile,
            senderMinecraftProfile: RPKMinecraftProfile?,
            message: String,
            isAsync: Boolean = false
    )

    /**
     * Sends a message to the channel, passing it through the specified directed pipeline once for each listener, and
     * the specified undirected pipeline once.
     *
     * @param sender The profile sending the message
     * @param senderMinecraftProfile The Minecraft profile used to send the message, or null if not sent from Minecraft
     * @param message The message
     * @param directedPreFormatPipeline The directed pre-format pipeline
     * @param directedPostFormatPipeline The directed post-format pipeline
     * @param undirectedPipeline The undirected pipeline
     * @param isAsync Whether the message is being sent asynchronously
     */
    fun sendMessage(
            sender: RPKThinProfile,
            senderMinecraftProfile: RPKMinecraftProfile?,
            message: String,
            directedPreFormatPipeline: List<DirectedPreFormatPipelineComponent>,
            format: List<FormatPart>,
            directedPostFormatPipeline: List<DirectedPostFormatPipelineComponent>,
            undirectedPipeline: List<UndirectedPipelineComponent>,
            isAsync: Boolean = false
    )

}