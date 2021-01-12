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

package com.rpkit.players.bukkit.profile.discord

import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKDiscordProfileTable
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl

class RPKDiscordProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKDiscordProfileService {
    override fun getDiscordProfile(id: RPKDiscordProfileId): RPKDiscordProfile? {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java).get(id)
    }

    override fun getDiscordProfile(discordUserId: DiscordUserId): RPKDiscordProfile {
        val discordProfileTable = plugin.database.getTable(RPKDiscordProfileTable::class.java)
        var discordProfile = discordProfileTable.get(discordUserId)
        if (discordProfile == null) {
            val discordService = Services[RPKDiscordService::class.java]
            val userName = discordService?.getUserName(discordUserId)
            discordProfile = RPKDiscordProfileImpl(discordId = discordUserId, profile = RPKThinProfileImpl(
                RPKProfileName(userName ?: "Unknown Discord User")
            ))
            discordProfileTable.insert(discordProfile)
        }
        return discordProfile
    }

    override fun getDiscordProfiles(profile: RPKProfile): List<RPKDiscordProfile> {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java).get(profile)
    }

    override fun addDiscordProfile(profile: RPKDiscordProfile) {
        plugin.database.getTable(RPKDiscordProfileTable::class.java).insert(profile)
    }

    override fun createDiscordProfile(profile: RPKThinProfile, discordId: DiscordUserId): RPKDiscordProfile {
        val discordProfile = RPKDiscordProfileImpl(
            null,
            profile,
            discordId
        )
        addDiscordProfile(discordProfile)
        return discordProfile
    }

    override fun updateDiscordProfile(profile: RPKDiscordProfile) {
        plugin.database.getTable(RPKDiscordProfileTable::class.java).update(profile)
    }

    override fun removeDiscordProfile(profile: RPKDiscordProfile) {
        plugin.database.getTable(RPKDiscordProfileTable::class.java).delete(profile)
    }
}