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

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_MINECRAFT_PROFILE
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import org.bukkit.OfflinePlayer
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.util.*


class RPKMinecraftProfileTable(private val database: Database, private val plugin: RPKPlayersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_minecraft_profile.id.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_minecraft_profile.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKMinecraftProfile::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_minecraft_profile.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKMinecraftProfile) {
        val profile = entity.profile
        database.create
                .insertInto(
                        RPKIT_MINECRAFT_PROFILE,
                        RPKIT_MINECRAFT_PROFILE.PROFILE_ID,
                        RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID
                )
                .values(
                        if (profile is RPKProfile) {
                            profile.id
                        } else {
                            null
                        },
                        entity.minecraftUUID.toString()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKMinecraftProfile) {
        val profile = entity.profile
        database.create
                .update(RPKIT_MINECRAFT_PROFILE)
                .set(
                        RPKIT_MINECRAFT_PROFILE.PROFILE_ID,
                        if (profile is RPKProfile) {
                            profile.id
                        } else {
                            null
                        }
                )
                .set(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID, entity.minecraftUUID.toString())
                .where(RPKIT_MINECRAFT_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKMinecraftProfile? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_MINECRAFT_PROFILE.PROFILE_ID,
                            RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID
                    )
                    .from(RPKIT_MINECRAFT_PROFILE)
                    .where(RPKIT_MINECRAFT_PROFILE.ID.eq(id))
                    .fetchOne() ?: return null
            val profileService = Services[RPKProfileService::class]
            val profileId = result.get(RPKIT_MINECRAFT_PROFILE.PROFILE_ID)
            val profile = if (profileId != null && profileService != null) {
                profileService.getProfile(profileId)
            } else {
                null
            } ?: RPKThinProfileImpl(
                    plugin.server.getOfflinePlayer(
                            UUID.fromString(result.get(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID))
                    ).name ?: "Unknown Minecraft user"
            )
            val minecraftProfile = RPKMinecraftProfileImpl(
                    id,
                    profile,
                    UUID.fromString(result.get(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID))
            )
            cache?.put(id, minecraftProfile)
            return minecraftProfile
        }
    }

    fun get(profile: RPKProfile): List<RPKMinecraftProfile> {
        val results = database.create
                .select(RPKIT_MINECRAFT_PROFILE.ID)
                .from(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.PROFILE_ID.eq(profile.id))
                .fetch()
        return results.map { result ->
            get(result.get(RPKIT_MINECRAFT_PROFILE.ID))
        }.filterNotNull()
    }

    fun get(player: OfflinePlayer): RPKMinecraftProfile? {
        val result = database.create
                .select(RPKIT_MINECRAFT_PROFILE.ID)
                .from(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID.eq(player.uniqueId.toString()))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_MINECRAFT_PROFILE.ID))
    }

    fun delete(entity: RPKMinecraftProfile) {
        database.create
                .deleteFrom(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}