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

package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_MINECRAFT_PROFILE
import com.rpkit.players.bukkit.profile.*
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileImpl
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


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

    fun insert(entity: RPKMinecraftProfile): CompletableFuture<Void> {
        val profile = entity.profile
        return CompletableFuture.runAsync {
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
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert Minecraft profile", exception)
            throw exception
        }
    }

    fun update(entity: RPKMinecraftProfile): CompletableFuture<Void> {
        val profile = entity.profile
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
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
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update Minecraft profile", exception)
            throw exception
        }
    }

    operator fun get(id: RPKMinecraftProfileId): CompletableFuture<out RPKMinecraftProfile?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_MINECRAFT_PROFILE.PROFILE_ID,
                        RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID
                    )
                    .from(RPKIT_MINECRAFT_PROFILE)
                    .where(RPKIT_MINECRAFT_PROFILE.ID.eq(id.value))
                    .fetchOne() ?: return@supplyAsync null
                val profileService = Services[RPKProfileService::class.java]
                val profileId = result.get(RPKIT_MINECRAFT_PROFILE.PROFILE_ID)
                val profile = if (profileId != null && profileService != null) {
                    profileService.getProfile(RPKProfileId(profileId)).join()
                } else {
                    null
                } ?: RPKThinProfileImpl(
                    RPKProfileName(
                        plugin.server.getOfflinePlayer(
                            UUID.fromString(result.get(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID))
                        ).name ?: "Unknown Minecraft user"
                    )
                )
                val minecraftProfile = RPKMinecraftProfileImpl(
                    id,
                    profile,
                    UUID.fromString(result.get(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID))
                )
                cache?.set(id.value, minecraftProfile)
                minecraftUUIDCache?.set(minecraftProfile.minecraftUUID, minecraftProfile)
                return@supplyAsync minecraftProfile
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get Minecraft profile", exception)
                throw exception
            }
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKMinecraftProfile>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_MINECRAFT_PROFILE.ID)
                .from(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.PROFILE_ID.eq(profileId.value))
                .fetch()
            val futures = results.map { result -> get(RPKMinecraftProfileId(result[RPKIT_MINECRAFT_PROFILE.ID])) }
            CompletableFuture.allOf(*futures.toTypedArray()).join()
            return@supplyAsync futures.mapNotNull(CompletableFuture<out RPKMinecraftProfile?>::join)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get Minecraft profiles", exception)
            throw exception
        }
    }

    operator fun get(minecraftUUID: UUID): CompletableFuture<RPKMinecraftProfile?> {
        if (minecraftUUIDCache?.containsKey(minecraftUUID) == true) {
            return CompletableFuture.completedFuture(minecraftUUIDCache[minecraftUUID])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_MINECRAFT_PROFILE.ID)
                .from(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.MINECRAFT_UUID.eq(minecraftUUID.toString()))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync get(RPKMinecraftProfileId(result.get(RPKIT_MINECRAFT_PROFILE.ID))).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get Minecraft profile", exception)
            throw exception
        }
    }

    fun delete(entity: RPKMinecraftProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_MINECRAFT_PROFILE)
                .where(RPKIT_MINECRAFT_PROFILE.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
            minecraftUUIDCache?.remove(entity.minecraftUUID)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete Minecraft profile", exception)
            throw exception
        }
    }
}