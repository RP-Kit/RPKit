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

package com.rpkit.players.bukkit.profile.discord

import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKDiscordProfileTable
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class RPKDiscordProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKDiscordProfileService {
    override fun getDiscordProfile(id: RPKDiscordProfileId): CompletableFuture<out RPKDiscordProfile?> {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java)[id]
    }

    override fun getDiscordProfile(discordUserId: DiscordUserId): CompletableFuture<RPKDiscordProfile> {
        return CompletableFuture.supplyAsync {
            val discordProfileTable = plugin.database.getTable(RPKDiscordProfileTable::class.java)
            var discordProfile = discordProfileTable.get(discordUserId).join()
            val discordService = Services[RPKDiscordService::class.java]
            val displayName = discordService?.getDisplayName(discordUserId)
            if (discordProfile == null) {
                discordProfile = RPKDiscordProfileImpl(
                    discordId = discordUserId, profile = RPKThinProfileImpl(
                        RPKProfileName(displayName ?: "Unknown Discord User")
                    )
                )
                discordProfileTable.insert(discordProfile).join()
            } else if (displayName != null && discordProfile.profile.name.value != displayName) {
                val profile = discordProfile.profile
                if (profile !is RPKProfile) {
                    discordProfile.profile = RPKThinProfileImpl(
                        RPKProfileName(displayName)
                    )
                }
            }
            return@supplyAsync discordProfile
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get Discord profile", exception)
            throw exception
        }
    }

    override fun getDiscordProfiles(profile: RPKProfile): CompletableFuture<List<RPKDiscordProfile>> {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java).get(profile)
    }

    override fun addDiscordProfile(profile: RPKDiscordProfile): CompletableFuture<Void> {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java).insert(profile)
    }

    override fun createDiscordProfile(profile: RPKThinProfile, discordId: DiscordUserId): CompletableFuture<RPKDiscordProfile> {
        return CompletableFuture.supplyAsync {
            val discordProfile = RPKDiscordProfileImpl(
                null,
                profile,
                discordId
            )
            addDiscordProfile(discordProfile).join()
            return@supplyAsync discordProfile
        }
    }

    override fun updateDiscordProfile(profile: RPKDiscordProfile): CompletableFuture<Void> {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java).update(profile)
    }

    override fun removeDiscordProfile(profile: RPKDiscordProfile): CompletableFuture<Void> {
        return plugin.database.getTable(RPKDiscordProfileTable::class.java).delete(profile)
    }
}