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

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile
import java.util.concurrent.CompletableFuture

interface RPKDiscordService : Service {

    fun sendMessage(channel: DiscordChannel, message: String, callback: DiscordMessageCallback? = null)
    fun sendMessage(profile: RPKDiscordProfile, message: String, callback: DiscordMessageCallback? = null)
    fun sendMessage(profile: RPKDiscordProfile, message: String, vararg buttons: DiscordButton)
    fun getUserName(discordId: DiscordUserId): String?
    fun getUserId(discordUserName: String): DiscordUserId?
    fun setMessageAsProfileLinkRequest(messageId: Long, profile: RPKProfile)
    fun setMessageAsProfileLinkRequest(message: DiscordMessage, profile: RPKProfile)
    fun getMessageProfileLink(messageId: Long): CompletableFuture<out RPKProfile?>
    fun getMessageProfileLink(message: DiscordMessage): CompletableFuture<out RPKProfile?>
    fun getDiscordChannel(name: String): DiscordChannel?
    fun disconnect()

}