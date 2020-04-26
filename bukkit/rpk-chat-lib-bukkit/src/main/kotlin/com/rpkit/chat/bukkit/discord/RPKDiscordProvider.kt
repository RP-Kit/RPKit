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

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

interface RPKDiscordProvider: ServiceProvider {

    fun sendMessage(channel: String, message: String)
    fun getUser(discordUserName: String): User?
    fun setMessageAsProfileLinkRequest(message: Message, profile: RPKProfile)
    fun setMessageAsProfileLinkRequest(messageId: String, profile: RPKProfile)
    fun getMessageProfileLink(message: Message): RPKProfile?
    fun getMessageProfileLink(messageId: String): RPKProfile?

}