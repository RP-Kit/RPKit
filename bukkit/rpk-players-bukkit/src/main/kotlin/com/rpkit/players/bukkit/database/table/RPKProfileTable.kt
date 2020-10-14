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
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKProfileTable(private val database: Database, private val plugin: RPKPlayersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_profile.id.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_profile.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfile::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_profile.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKProfile) {
        database.create
                .insertInto(
                        RPKIT_PROFILE,
                        RPKIT_PROFILE.NAME,
                        RPKIT_PROFILE.PASSWORD_HASH,
                        RPKIT_PROFILE.PASSWORD_SALT
                )
                .values(
                        entity.name,
                        entity.passwordHash,
                        entity.passwordSalt
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKProfile) {
        database.create
                .update(RPKIT_PROFILE)
                .set(RPKIT_PROFILE.NAME, entity.name)
                .set(RPKIT_PROFILE.PASSWORD_HASH, entity.passwordHash)
                .set(RPKIT_PROFILE.PASSWORD_SALT, entity.passwordSalt)
                .where(RPKIT_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKProfile? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE.NAME,
                            RPKIT_PROFILE.PASSWORD_HASH,
                            RPKIT_PROFILE.PASSWORD_SALT
                    )
                    .from(RPKIT_PROFILE)
                    .where(RPKIT_PROFILE.ID.eq(id))
                    .fetchOne() ?: return null
            val profile = RPKProfileImpl(
                    id,
                    result.get(RPKIT_PROFILE.NAME),
                    result.get(RPKIT_PROFILE.PASSWORD_HASH),
                    result.get(RPKIT_PROFILE.PASSWORD_SALT)
            )
            cache?.put(id, profile)
            return profile
        }
    }

    fun get(name: String): RPKProfile? {
        val result = database.create
                .select(RPKIT_PROFILE.ID)
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_PROFILE.ID))
    }

    fun delete(entity: RPKProfile) {
        database.create
                .deleteFrom(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}