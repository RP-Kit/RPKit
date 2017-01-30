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

import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer
import java.awt.Color

/**
 * Represents a chat channel
 */
interface RPKChatChannel: Entity {

    /**
     * The name of the chat channel.
     */
    val name: String

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
     * Players may only be speakers in a single channel.
     */
    val speakers: List<RPKPlayer>

    /**
     * A list of all listeners in the channel.
     * If a message is sent to a channel, it will be heard by all listeners.
     * Players may listen to multiple channels.
     */
    val listeners: List<RPKPlayer>

    /**
     * The directed pipeline for the channel.
     * Messages to this channel will pass through this pipeline for each listener, so that formatting may be applied for
     * each recipient.
     */
    val directedPipeline: List<DirectedChatChannelPipelineComponent>

    /**
     * The undirected pipeline for the channel.
     * Messages to this channel will pass through this pipeline once, so that messages may be logged or sent to IRC only
     * a single time.
     */
    val undirectedPipeline: List<UndirectedChatChannelPipelineComponent>

    /**
     * The match pattern for this channel.
     * If a player's message matches the match pattern, it should be directed to this channel.
     * In the case of a chat channel not having a match pattern, this may be set to null.
     */
    val matchPattern: String?

    /**
     * Whether this channel should be joined by default.
     * If a channel is joined by default, all new players are added as listeners to the channel upon joining for the
     * first time. If they are not, the channel is muted until they join it.
     */
    val isJoinedByDefault: Boolean

    /**
     * Adds a speaker to the channel.
     *
     * @param speaker The player to add
     */
    fun addSpeaker(speaker: RPKPlayer)

    /**
     * Removes a speaker from the channel.
     *
     * @param speaker The player to remove
     */
    fun removeSpeaker(speaker: RPKPlayer)

    /**
     * Adds a listener to the channel.
     *
     * @param listener The player to add
     */
    fun addListener(listener: RPKPlayer)

    /**
     * Removes a listener from the channel.
     *
     * @param listener The player to remove
     */
    fun removeListener(listener: RPKPlayer)

    /**
     * Sends a message to the channel, passing it through the directed pipeline once for each listener, and the
     * undirected pipeline once.
     *
     * @param sender The player sending the message
     * @param message The message
     */
    fun sendMessage(sender: RPKPlayer, message: String)

    /**
     * Sends the message to the channel, passing it through the specified directed pipeline once for each listener, and
     * the specified undirected pipeline once.
     *
     * @param sender The player sending the message
     * @param message The message
     * @param directedPipeline The directed pipeline
     * @param undirectedPipeline The undirected pipeline
     */
    fun sendMessage(sender: RPKPlayer, message: String, directedPipeline: List<DirectedChatChannelPipelineComponent>, undirectedPipeline: List<UndirectedChatChannelPipelineComponent>) {
        sendMessage(sender, message)
    }

}