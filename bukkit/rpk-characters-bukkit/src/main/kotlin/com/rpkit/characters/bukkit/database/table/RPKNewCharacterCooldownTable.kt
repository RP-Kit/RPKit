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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.jooq.Tables.RPKIT_NEW_CHARACTER_COOLDOWN
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldown
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKNewCharacterCooldownTable(private val database: Database, private val plugin: RPKCharactersBukkit) : Table {

    private val profileCache = if (plugin.config.getBoolean("caching.rpkit_new_character_cooldown.profile_id.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_new_character_cooldown.profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKNewCharacterCooldown::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_new_character_cooldown.profile_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKNewCharacterCooldown) {
        database.create
                .insertInto(
                        RPKIT_NEW_CHARACTER_COOLDOWN,
                        RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID,
                        RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .values(
                        entity.profile.id,
                        entity.cooldownExpiryTime
                )
                .execute()
        profileCache?.put(entity.profile.id, entity)
    }

    fun update(entity: RPKNewCharacterCooldown) {
        database.create
                .update(RPKIT_NEW_CHARACTER_COOLDOWN)
                .set(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP, entity.cooldownExpiryTime)
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(entity.profile.id))
                .execute()
        profileCache?.put(entity.profile.id, entity)
    }

    operator fun get(profile: RPKProfile): RPKNewCharacterCooldown? {
        if (profileCache?.containsKey(profile.id) == true) {
            return profileCache[profile.id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID,
                            RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                    )
                    .from(RPKIT_NEW_CHARACTER_COOLDOWN)
                    .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(profile.id))
                    .fetchOne() ?: return null
            val newCharacterCooldown = RPKNewCharacterCooldown(
                    profile,
                    result.get(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP)
            )
            profileCache?.put(newCharacterCooldown.profile.id, newCharacterCooldown)
            return newCharacterCooldown
        }
    }

    fun delete(entity: RPKNewCharacterCooldown) {
        database.create
                .deleteFrom(RPKIT_NEW_CHARACTER_COOLDOWN)
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(entity.profile.id))
                .execute()
        profileCache?.remove(entity.profile.id)
    }

}