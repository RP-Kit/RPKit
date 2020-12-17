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
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import org.jooq.impl.DSL.max


class RPKProfileTable(private val database: Database, private val plugin: RPKPlayersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_profile.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_profile.id",
            Int::class.javaObjectType,
            RPKProfile::class.java,
            plugin.config.getLong("caching.rpkit_profile.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKProfile) {
        database.create
                .insertInto(
                        RPKIT_PROFILE,
                        RPKIT_PROFILE.NAME,
                        RPKIT_PROFILE.DISCRIMINATOR,
                        RPKIT_PROFILE.PASSWORD_HASH,
                        RPKIT_PROFILE.PASSWORD_SALT
                )
                .values(
                        entity.name,
                        entity.discriminator,
                        entity.passwordHash,
                        entity.passwordSalt
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.set(id, entity)
    }

    fun update(entity: RPKProfile) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_PROFILE)
                .set(RPKIT_PROFILE.NAME, entity.name)
                .set(RPKIT_PROFILE.DISCRIMINATOR, entity.discriminator)
                .set(RPKIT_PROFILE.PASSWORD_HASH, entity.passwordHash)
                .set(RPKIT_PROFILE.PASSWORD_SALT, entity.passwordSalt)
                .where(RPKIT_PROFILE.ID.eq(id))
                .execute()
        cache?.set(id, entity)
    }

    operator fun get(id: Int): RPKProfile? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE.NAME,
                            RPKIT_PROFILE.DISCRIMINATOR,
                            RPKIT_PROFILE.PASSWORD_HASH,
                            RPKIT_PROFILE.PASSWORD_SALT
                    )
                    .from(RPKIT_PROFILE)
                    .where(RPKIT_PROFILE.ID.eq(id))
                    .fetchOne() ?: return null
            val profile = RPKProfileImpl(
                    id,
                    result.get(RPKIT_PROFILE.NAME),
                    result.get(RPKIT_PROFILE.DISCRIMINATOR),
                    result.get(RPKIT_PROFILE.PASSWORD_HASH),
                    result.get(RPKIT_PROFILE.PASSWORD_SALT)
            )
            cache?.set(id, profile)
            return profile
        }
    }

    fun get(name: String, discriminator: Int): RPKProfile? {
        val result = database.create
                .select(RPKIT_PROFILE.ID)
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name))
                .and(RPKIT_PROFILE.DISCRIMINATOR.eq(discriminator))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_PROFILE.ID))
    }

    fun delete(entity: RPKProfile) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.ID.eq(id))
                .execute()
        cache?.remove(id)
    }

    fun generateDiscriminatorFor(name: String): Int {
        val result = database.create
                .select(max(RPKIT_PROFILE.DISCRIMINATOR))
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name))
                .fetchOne()
        return result?.get(0, Int::class.javaObjectType)?.plus(1) ?: 1
    }

}