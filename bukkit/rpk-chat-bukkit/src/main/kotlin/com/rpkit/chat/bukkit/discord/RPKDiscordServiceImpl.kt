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

package com.rpkit.chat.bukkit.discord

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile

class RPKDiscordServiceImpl(override val plugin: RPKChatBukkit) : RPKDiscordService {

    private val serverName = plugin.config.getString("discord.server-name")
    val discordServer = if (serverName != null) DiscordServer(plugin, serverName) else null
    private val profileLinkMessages = mutableMapOf<Long, Int>()

    override fun sendMessage(channel: DiscordChannel, message: String, callback: DiscordMessageCallback?) {
        discordServer?.sendMessage(channel, message)
    }

    override fun sendMessage(
            profile: RPKDiscordProfile,
            message: String,
            callback: DiscordMessageCallback?
    ) {
        discordServer?.getUser(profile.discordId)?.openPrivateChannel()?.queue { channel ->
            channel.sendMessage(message).queue {
                callback?.invoke(DiscordMessageImpl(it))
            }
        }
    }

    override fun getUserName(discordId: DiscordUserId): String? {
        return discordServer?.getUser(discordId)?.name
    }

    override fun getUserId(discordUserName: String): DiscordUserId? {
        return discordServer?.getUser(discordUserName)?.idLong?.let(::DiscordUserId)
    }

    override fun setMessageAsProfileLinkRequest(messageId: Long, profile: RPKProfile) {
        val profileId = profile.id ?: return
        profileLinkMessages[messageId] = profileId.value
    }

    override fun setMessageAsProfileLinkRequest(message: DiscordMessage, profile: RPKProfile) {
        setMessageAsProfileLinkRequest(message.id, profile)
    }

    override fun getMessageProfileLink(messageId: Long): RPKProfile? {
        val profileService = Services[RPKProfileService::class.java] ?: return null
        val profileId = profileLinkMessages[messageId] ?: return null
        return profileService.getProfile(RPKProfileId(profileId))
    }

    override fun getMessageProfileLink(message: DiscordMessage): RPKProfile? {
        return getMessageProfileLink(message.id)
    }

    override fun getDiscordChannel(name: String): DiscordChannel? {
        return discordServer?.getChannel(name)
    }

}