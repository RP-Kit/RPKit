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

package com.rpkit.players.bukkit.database.table

import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_DISCORD_PROFILE
import com.rpkit.players.bukkit.profile.RPKDiscordProfile
import com.rpkit.players.bukkit.profile.RPKDiscordProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import net.dv8tion.jda.api.entities.User
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class RPKDiscordProfileTable(
        private val database: Database,
        plugin: RPKPlayersBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_discord_profile.id.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_discord_profile.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKDiscordProfile::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_discord_profile.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKDiscordProfile) {
        val profile = entity.profile
        database.create
                .insertInto(
                        RPKIT_DISCORD_PROFILE,
                        RPKIT_DISCORD_PROFILE.PROFILE_ID,
                        RPKIT_DISCORD_PROFILE.DISCORD_ID
                )
                .values(
                        if (profile is RPKProfile) {
                            profile.id
                        } else {
                            null
                        },
                        entity.discordId
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKDiscordProfile) {
        val profile = entity.profile
        database.create
                .update(RPKIT_DISCORD_PROFILE)
                .set(
                        RPKIT_DISCORD_PROFILE.PROFILE_ID,
                        if (profile is RPKProfile) {
                            profile.id
                        } else {
                            null
                        }
                )
                .set(RPKIT_DISCORD_PROFILE.DISCORD_ID, entity.discordId)
                .where(RPKIT_DISCORD_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKDiscordProfile? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_DISCORD_PROFILE.PROFILE_ID,
                        RPKIT_DISCORD_PROFILE.DISCORD_ID
                )
                .from(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.ID.eq(id))
                .fetchOne() ?: return null
        val profileId = result[RPKIT_DISCORD_PROFILE.PROFILE_ID]
        val profileService = Services[RPKProfileService::class] ?: return null
        val discordService = Services[RPKDiscordService::class] ?: return null
        val profile = if (profileId != null) {
            profileService.getProfile(profileId)
        } else {
            null
        } ?: RPKThinProfileImpl(discordService.getUser(result[RPKIT_DISCORD_PROFILE.DISCORD_ID])?.name
                ?: "Unknown Discord user")
        val discordProfile = RPKDiscordProfileImpl(
                id,
                profile,
                result[RPKIT_DISCORD_PROFILE.DISCORD_ID]
        )
        cache?.put(id, discordProfile)
        return discordProfile
    }

    fun get(user: User): RPKDiscordProfile? {
        val result = database.create
                .select(RPKIT_DISCORD_PROFILE.ID)
                .from(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.DISCORD_ID.eq(user.idLong))
                .fetchOne() ?: return null
        return get(result[RPKIT_DISCORD_PROFILE.ID])
    }

    fun get(profile: RPKProfile): List<RPKDiscordProfile> {
        val results = database.create
                .select(RPKIT_DISCORD_PROFILE.PROFILE_ID)
                .from(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.PROFILE_ID.eq(profile.id))
                .fetch()
        return results.mapNotNull { result ->
            get(result[RPKIT_DISCORD_PROFILE.ID])
        }
    }

    fun delete(entity: RPKDiscordProfile) {
        database.create
                .deleteFrom(RPKIT_DISCORD_PROFILE)
                .where(RPKIT_DISCORD_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }


}