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

import com.rpkit.chat.bukkit.chatchannel.matchpattern.RPKChatChannelMatchPattern
import com.rpkit.chat.bukkit.discord.DiscordChannel
import com.rpkit.chat.bukkit.irc.IRCChannel
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile

/**
 * Provides chat channel related operations.
 */
interface RPKChatChannelService : Service {

    /**
     * A collection of all chat channels currently managed by this chat channel service.
     * The collection is immutable.
     */
    val chatChannels: Collection<RPKChatChannel>

    /**
     * Gets a chat channel by name.
     * If there is no chat channel with the given name, null is returned.
     *
     * @param name The name of the chat channel
     * @return The chat channel, or null if no chat channel is found with the given name
     */
    fun getChatChannel(name: RPKChatChannelName): RPKChatChannel?

    /**
     * Gets the chat channel a Minecraft profile is currently speaking in.
     * If the Minecraft profile is not currently speaking in a channel, null is returned.
     *
     * @param minecraftProfile The Minecraft profile
     */
    fun getMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile): RPKChatChannel?

    /**
     * Sets the chat channel a Minecraft profile is currently speaking in.
     *
     * @param minecraftProfile The Minecraft profile
     * @param channel The channel to set
     */
    fun setMinecraftProfileChannel(minecraftProfile: RPKMinecraftProfile, channel: RPKChatChannel?, isAsync: Boolean = false)

    /**
     * Gets a chat channel from the IRC channel it is linked to.
     * If no chat channel uses the given IRC channel, null is returned.
     *
     * @param ircChannel The IRC channel
     * @return The chat channel, or null if no chat channel uses the given IRC channel.
     */
    fun getChatChannelFromIRCChannel(ircChannel: IRCChannel): RPKChatChannel?

    /**
     * Gets a chat channel from the Discord channel it is linked to.
     * If no chat channel uses the given Discord channel, null is returned.
     *
     * @param discordChannel The Discord channel
     * @return The chat channel, or null if no chat channel uses the given Discord channel.
     */
    fun getChatChannelFromDiscordChannel(discordChannel: DiscordChannel): RPKChatChannel?

    val matchPatterns: List<RPKChatChannelMatchPattern>
}