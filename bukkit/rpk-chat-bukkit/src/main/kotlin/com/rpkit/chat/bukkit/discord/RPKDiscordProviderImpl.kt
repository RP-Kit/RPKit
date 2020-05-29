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

package com.rpkit.chat.bukkit.discord

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

class RPKDiscordProviderImpl(private val plugin: RPKChatBukkit): RPKDiscordProvider {

    private val serverName = plugin.config.getString("discord.server-name")
    val discordServer = if (serverName != null) DiscordServer(plugin, serverName) else null
    private val profileLinkMessages = mutableMapOf<String, Int>()

    override fun sendMessage(channel: String, message: String) {
        discordServer?.sendMessage(channel, message)
    }

    override fun getUser(discordId: Long): User? {
        return discordServer?.getUser(discordId)
    }

    override fun getUser(discordUserName: String): User? {
        return discordServer?.getUser(discordUserName)
    }

    override fun setMessageAsProfileLinkRequest(message: Message, profile: RPKProfile) {
        setMessageAsProfileLinkRequest(message.id, profile)
    }

    override fun setMessageAsProfileLinkRequest(messageId: String, profile: RPKProfile) {
        profileLinkMessages[messageId] = profile.id
    }

    override fun getMessageProfileLink(message: Message): RPKProfile? {
        return getMessageProfileLink(message.id)
    }

    override fun getMessageProfileLink(messageId: String): RPKProfile? {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profileId = profileLinkMessages[messageId] ?: return null
        return profileProvider.getProfile(profileId)
    }

}