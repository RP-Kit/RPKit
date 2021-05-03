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

package com.rpkit.players.bukkit.database.table

import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_DISCORD_PROFILE
import com.rpkit.players.bukkit.profile.*
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileImpl
import java.util.concurrent.CompletableFuture

class RPKDiscordProfileTable(
        private val database: Database,
        plugin: RPKPlayersBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_discord_profile.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_discord_profile.id",
            Int::class.javaObjectType,
            RPKDiscordProfile::class.java,
            plugin.config.getLong("caching.rpkit_discord_profile.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKDiscordProfile): CompletableFuture<Void> {
        val profile = entity.profile
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_DISCORD_PROFILE,
                    RPKIT_DISCORD_PROFILE.PROFILE_ID,
                    RPKIT_DISCORD_PROFILE.DISCORD_ID
                )
                .values(
                    if (profile is RPKProfile) {
                        profile.id?.value
                    } else {
                        null
                    },
                    entity.discordId.value
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKDiscordProfileId(id)
            cache?.set(id, entity)
        }
    }

    fun update(entity: RPKDiscordProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        val profile = entity.profile
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_DISCORD_PROFILE)
                .set(
                    RPKIT_DISCORD_PROFILE.PROFILE_ID,
                    if (profile is RPKProfile) {
                        profile.id?.value
                    } else {
                        null
                    }
                )
                .set(RPKIT_DISCORD_PROFILE.DISCORD_ID, entity.discordId.value)
                .where(RPKIT_DISCORD_PROFILE.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }
    }

    operator fun get(id: RPKDiscordProfileId): CompletableFuture<RPKDiscordProfile?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_DISCORD_PROFILE.PROFILE_ID,
                    RPKIT_DISCORD_PROFILE.DISCORD_ID
                )
                .from(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.ID.eq(id.value))
                .fetchOne() ?: return@supplyAsync null
            val profileId = result[RPKIT_DISCORD_PROFILE.PROFILE_ID]
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync null
            val discordService = Services[RPKDiscordService::class.java] ?: return@supplyAsync null
            val profile = if (profileId != null) {
                profileService.getProfile(RPKProfileId(profileId)).join()
            } else {
                null
            } ?: RPKThinProfileImpl(
                RPKProfileName(discordService.getUserName(DiscordUserId(result[RPKIT_DISCORD_PROFILE.DISCORD_ID]))
                    ?: "Unknown Discord user")
            )
            val discordProfile = RPKDiscordProfileImpl(
                id,
                profile,
                DiscordUserId(result[RPKIT_DISCORD_PROFILE.DISCORD_ID])
            )
            cache?.set(id.value, discordProfile)
            return@supplyAsync discordProfile
        }
    }

    fun get(userId: DiscordUserId): CompletableFuture<RPKDiscordProfile?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_DISCORD_PROFILE.ID)
                .from(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.DISCORD_ID.eq(userId.value))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync get(RPKDiscordProfileId(result[RPKIT_DISCORD_PROFILE.ID])).join()
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKDiscordProfile>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_DISCORD_PROFILE.ID)
                .from(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.PROFILE_ID.eq(profileId.value))
                .fetch()
            val discordProfileFutures = results.map { result ->
                get(RPKDiscordProfileId(result[RPKIT_DISCORD_PROFILE.ID]))
            }
            CompletableFuture.allOf(*discordProfileFutures.toTypedArray()).join()
            return@supplyAsync discordProfileFutures.mapNotNull(CompletableFuture<RPKDiscordProfile?>::join)
        }
    }

    fun delete(entity: RPKDiscordProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }
    }


}