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
import com.rpkit.locks.bukkit.database.jooq.Tables.RPKIT_PLAYER_GETTING_KEY
import com.rpkit.locks.bukkit.database.jooq.tables.records.RpkitPlayerGettingKeyRecord
import com.rpkit.locks.bukkit.lock.RPKPlayerGettingKey
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKPlayerGettingKeyTable(private val database: Database, private val plugin: RPKLocksBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_player_getting_key.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-locks-bukkit.rpkit_player_getting_key.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKPlayerGettingKey::class.java,
            plugin.config.getLong("caching.rpkit_player_getting_key.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPlayerGettingKey): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_PLAYER_GETTING_KEY,
                    RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID
                )
                .values(minecraftProfileId.value)
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert player getting key", exception)
            throw exception
        }
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKPlayerGettingKey?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(cache[minecraftProfileId.value])
        }
        return CompletableFuture.supplyAsync {
            database.create
                .select(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID)
                .from(RPKIT_PLAYER_GETTING_KEY)
                .where(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetchOne() ?: return@supplyAsync null
            val playerGettingKey = RPKPlayerGettingKey(
                minecraftProfile
            )
            cache?.set(minecraftProfileId.value, playerGettingKey)
            return@supplyAsync playerGettingKey
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get player getting key", exception)
            throw exception
        }
    }

    fun getAll(): CompletableFuture<List<RPKPlayerGettingKey>> {
        return CompletableFuture.supplyAsync {
            database.create
                .selectFrom(RPKIT_PLAYER_GETTING_KEY)
                .fetch()
                .map { it.toDomain() }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get all players getting keys", exception)
            throw exception
        }
    }

    fun delete(entity: RPKPlayerGettingKey): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_PLAYER_GETTING_KEY)
                .where(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.remove(minecraftProfileId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete player getting key", exception)
            throw exception
        }
    }

    private fun RpkitPlayerGettingKeyRecord.toDomain() = Services[RPKMinecraftProfileService::class.java]?.getMinecraftProfile(
        RPKMinecraftProfileId(minecraftProfileId)
    )?.thenApply { minecraftProfile ->
        minecraftProfile?.let { RPKPlayerGettingKey(minecraftProfile = it) }
    }?.join()

}