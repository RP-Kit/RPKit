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

package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.create
import com.rpkit.locks.bukkit.database.jooq.Tables.RPKIT_PLAYER_UNCLAIMING
import com.rpkit.locks.bukkit.database.jooq.tables.records.RpkitPlayerUnclaimingRecord
import com.rpkit.locks.bukkit.lock.RPKPlayerUnclaiming
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level.SEVERE


class RPKPlayerUnclaimingTable(private val database: Database, private val plugin: RPKLocksBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_player_unclaiming.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-locks-bukkit.rpkit_player_unclaiming.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKPlayerUnclaiming::class.java,
            plugin.config.getLong("caching.rpkit_player_unclaiming.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPlayerUnclaiming): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_PLAYER_UNCLAIMING,
                    RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID
                )
                .values(
                    minecraftProfileId.value
                )
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to insert player unclaiming", exception)
            throw exception
        }
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKPlayerUnclaiming?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(cache[minecraftProfileId.value])
        } else {
            return CompletableFuture.supplyAsync {
                database.create
                    .select(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_PLAYER_UNCLAIMING)
                    .where(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .fetchOne() ?: return@supplyAsync null
                val playerUnclaiming = RPKPlayerUnclaiming(minecraftProfile)
                cache?.set(minecraftProfileId.value, playerUnclaiming)
                return@supplyAsync playerUnclaiming
            }.exceptionally { exception ->
                plugin.logger.log(SEVERE, "Failed to get player unclaiming", exception)
                throw exception
            }
        }
    }

    fun getAll(): CompletableFuture<List<RPKPlayerUnclaiming>> {
        return CompletableFuture.supplyAsync {
            database.create
                .selectFrom(RPKIT_PLAYER_UNCLAIMING)
                .fetch()
                .map { it.toDomain() }
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to get all players unclaiming", exception)
            throw exception
        }
    }

    fun delete(entity: RPKPlayerUnclaiming): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_PLAYER_UNCLAIMING)
                .where(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.remove(minecraftProfileId.value)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to delete player unclaiming", exception)
            throw exception
        }
    }

    fun delete(minecraftProfileId: RPKMinecraftProfileId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_PLAYER_UNCLAIMING)
            .where(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .execute()
        cache?.remove(minecraftProfileId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete player unclaiming for Minecraft profile id", exception)
        throw exception
    }

    private fun RpkitPlayerUnclaimingRecord.toDomain() = Services[RPKMinecraftProfileService::class.java]?.getMinecraftProfile(
        RPKMinecraftProfileId(minecraftProfileId)
    )?.thenApply { minecraftProfile ->
        minecraftProfile?.let { RPKPlayerUnclaiming(minecraftProfile = it) }
    }?.join()

}