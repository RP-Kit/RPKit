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

package com.rpkit.moderation.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.create
import com.rpkit.moderation.bukkit.database.jooq.Tables.RPKIT_VANISHED
import com.rpkit.moderation.bukkit.vanish.RPKVanishState
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile


class RPKVanishStateTable(private val database: Database, private val plugin: RPKModerationBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_vanished.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-moderation-bukkit.rpkit_vanished.id",
            Int::class.javaObjectType,
            RPKVanishState::class.java,
            plugin.config.getLong("caching.rpkit_vanished.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKVanishState) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .insertInto(
                        RPKIT_VANISHED,
                        RPKIT_VANISHED.MINECRAFT_PROFILE_ID
                )
                .values(minecraftProfileId.value)
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): RPKVanishState? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return cache[minecraftProfileId.value]
        } else {
            database.create
                    .select(RPKIT_VANISHED.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_VANISHED)
                    .where(RPKIT_VANISHED.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .fetchOne() ?: return null
            val vanishState = RPKVanishState(
                    minecraftProfile
            )
            cache?.set(minecraftProfileId.value, vanishState)
            return vanishState
        }
    }

    fun delete(entity: RPKVanishState) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .deleteFrom(RPKIT_VANISHED)
                .where(RPKIT_VANISHED.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.remove(minecraftProfileId.value)
    }

}