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

package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.create
import com.rpkit.locks.bukkit.database.jooq.Tables.RPKIT_PLAYER_UNCLAIMING
import com.rpkit.locks.bukkit.lock.RPKPlayerUnclaiming
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile


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

    fun insert(entity: RPKPlayerUnclaiming) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .insertInto(
                        RPKIT_PLAYER_UNCLAIMING,
                        RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID
                )
                .values(
                        minecraftProfileId
                )
                .execute()
        cache?.set(minecraftProfileId, entity)
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): RPKPlayerUnclaiming? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        if (cache?.containsKey(minecraftProfileId) == true) {
            return cache[minecraftProfileId]
        } else {
            database.create
                    .select(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_PLAYER_UNCLAIMING)
                    .where(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetchOne() ?: return null
            val playerUnclaiming = RPKPlayerUnclaiming(minecraftProfile)
            cache?.set(minecraftProfileId, playerUnclaiming)
            return playerUnclaiming
        }
    }

    fun delete(entity: RPKPlayerUnclaiming) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .deleteFrom(RPKIT_PLAYER_UNCLAIMING)
                .where(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.remove(minecraftProfileId)
    }

}