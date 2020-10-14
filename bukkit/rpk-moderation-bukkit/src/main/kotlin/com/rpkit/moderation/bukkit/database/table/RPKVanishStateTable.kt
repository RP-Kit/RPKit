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

package com.rpkit.moderation.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.jooq.Tables.RPKIT_VANISHED
import com.rpkit.moderation.bukkit.vanish.RPKVanishState
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKVanishStateTable(private val database: Database, private val plugin: RPKModerationBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_vanished.id.enabled")) {
        database.cacheManager.createCache("rpk-moderation-bukkit.rpkit_vanished.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKVanishState::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_vanished.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKVanishState) {
        database.create
                .insertInto(
                        RPKIT_VANISHED,
                        RPKIT_VANISHED.MINECRAFT_PROFILE_ID
                )
                .values(entity.minecraftProfile.id)
                .execute()
        cache?.put(entity.minecraftProfile.id, entity)
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): RPKVanishState? {
        if (cache?.containsKey(minecraftProfile.id) == true) {
            return cache[minecraftProfile.id]
        } else {
            database.create
                    .select(RPKIT_VANISHED.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_VANISHED)
                    .where(RPKIT_VANISHED.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetchOne() ?: return null
            val vanishState = RPKVanishState(
                    minecraftProfile
            )
            cache?.put(minecraftProfile.id, vanishState)
            return vanishState
        }
    }

    fun delete(entity: RPKVanishState) {
        database.create
                .deleteFrom(RPKIT_VANISHED)
                .where(RPKIT_VANISHED.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.remove(entity.minecraftProfile.id)
    }

}