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

package com.rpkit.chat.bukkit.discord

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.CompletableFuture

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

    override fun sendMessage(
        profile: RPKDiscordProfile,
        message: String,
        vararg buttons: DiscordButton
    ) {
        discordServer
            ?.getUser(profile.discordId)
            ?.openPrivateChannel()?.queue { channel ->
                channel.sendMessage(message).addActionRow(buttons.map { button ->
                    val style = when (button.variant) {
                        DiscordButton.Variant.PRIMARY -> ButtonStyle.PRIMARY
                        DiscordButton.Variant.SUCCESS -> ButtonStyle.SUCCESS
                        DiscordButton.Variant.SECONDARY -> ButtonStyle.SECONDARY
                        DiscordButton.Variant.DANGER -> ButtonStyle.DANGER
                        DiscordButton.Variant.LINK -> ButtonStyle.LINK
                    }
                    discordServer.addButtonListener(button.id, button.onClick)
                    when (button) {
                        is DiscordTextButton -> Button.of(style, button.id, button.text)
                        is DiscordEmojiButton -> Button.of(style, button.id, Emoji.fromUnicode(button.emoji))
                    }
            }).queue()
        }
    }

    override fun getUserName(discordId: DiscordUserId): String? {
        return discordServer?.getUser(discordId)?.name
    }

    override fun getDisplayName(discordId: DiscordUserId): String? {
        return discordServer?.guild?.getMemberById(discordId.value)?.effectiveName
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

    override fun getMessageProfileLink(messageId: Long): CompletableFuture<out RPKProfile?> {
        val profileService = Services[RPKProfileService::class.java] ?: return CompletableFuture.completedFuture(null)
        val profileId = profileLinkMessages[messageId] ?: return CompletableFuture.completedFuture(null)
        return profileService.getProfile(RPKProfileId(profileId))
    }

    override fun getMessageProfileLink(message: DiscordMessage): CompletableFuture<out RPKProfile?> {
        return getMessageProfileLink(message.id)
    }

    override fun getDiscordChannel(name: String): DiscordChannel? {
        return discordServer?.getChannel(name)
    }

    override fun disconnect() {
        discordServer?.disconnect()
    }

}