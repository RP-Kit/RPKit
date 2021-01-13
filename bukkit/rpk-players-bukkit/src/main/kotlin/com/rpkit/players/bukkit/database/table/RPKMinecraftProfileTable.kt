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

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_MINECRAFT_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileImpl
import java.util.UUID


class RPKMinecraftProfileTable(private val database: Database, private val plugin: RPKPlayersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_minecraft_profile.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_minecraft_profile.id",
            Int::class.javaObjectType,
            RPKMinecraftProfile::class.java,
            plugin.config.getLong("caching.rpkit_minecraft_profile.id.size")
        )
    } else {
        null
    }

    private val minecraftUUIDCache = if (plugin.config.getBoolean("caching.rpkit_minecraft_profile.minecraft_uuid.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_minecraft_profile.minecraft_uuid",
            UUID::class.java,
            RPKMinecraftProfile::class.java,
            plugin.config.getLong("caching.rpkit_minecraft_profile.minecraft_uuid.size")
        )
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
                            profile.id?.value
                        } else {
                            null
                        },
                        entity.minecraftUUID.toString()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKMinecraftProfileId(id)
        cache?.set(id, entity)
        minecraftUUIDCache?.set(entity.minecraftUUID, entity)
    }

    fun update(entity: RPKMinecraftProfile) {
        val profile = entity.profile
        val id = entity.id ?: return
        database.create
                .update(RPKIT_MINECRAFT_PROFILE)
                .set(
                        RPKIT_MINECRAFT_PROFILE.PROFILE_ID,
                        if (profile is RPKProfile) {
                            profile.id?.value
                        } else {
                            null
                        }
                )
                .set(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID, entity.minecraftUUID.toString())
                .where(RPKIT_MINECRAFT_PROFILE.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
        minecraftUUIDCache?.set(entity.minecraftUUID, entity)
    }

    operator fun get(id: RPKMinecraftProfileId): RPKMinecraftProfile? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        } else {
            val result = database.create
                    .select(
                            RPKIT_MINECRAFT_PROFILE.PROFILE_ID,
                            RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID
                    )
                    .from(RPKIT_MINECRAFT_PROFILE)
                    .where(RPKIT_MINECRAFT_PROFILE.ID.eq(id.value))
                    .fetchOne() ?: return null
            val profileService = Services[RPKProfileService::class.java]
            val profileId = result.get(RPKIT_MINECRAFT_PROFILE.PROFILE_ID)
            val profile = if (profileId != null && profileService != null) {
                profileService.getProfile(RPKProfileId(profileId))
            } else {
                null
            } ?: RPKThinProfileImpl(
                    RPKProfileName(plugin.server.getOfflinePlayer(
                            UUID.fromString(result.get(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID))
                    ).name ?: "Unknown Minecraft user")
            )
            val minecraftProfile = RPKMinecraftProfileImpl(
                    id,
                    profile,
                    UUID.fromString(result.get(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID))
            )
            cache?.set(id.value, minecraftProfile)
            minecraftUUIDCache?.set(minecraftProfile.minecraftUUID, minecraftProfile)
            return minecraftProfile
        }
    }

    fun get(profile: RPKProfile): List<RPKMinecraftProfile> {
        val profileId = profile.id ?: return emptyList()
        val results = database.create
                .select(RPKIT_MINECRAFT_PROFILE.ID)
                .from(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.PROFILE_ID.eq(profileId.value))
                .fetch()
        return results.map { result ->
            get(RPKMinecraftProfileId(result.get(RPKIT_MINECRAFT_PROFILE.ID)))
        }.filterNotNull()
    }

    fun get(minecraftUUID: UUID): RPKMinecraftProfile? {
        if (minecraftUUIDCache?.containsKey(minecraftUUID) == true) {
            return minecraftUUIDCache[minecraftUUID]
        }
        val result = database.create
                .select(RPKIT_MINECRAFT_PROFILE.ID)
                .from(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID.eq(minecraftUUID.toString()))
                .fetchOne() ?: return null
        return get(RPKMinecraftProfileId(result.get(RPKIT_MINECRAFT_PROFILE.ID)))
    }

    fun delete(entity: RPKMinecraftProfile) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
        minecraftUUIDCache?.remove(entity.minecraftUUID)
    }
}