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
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileName
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
                        entity.name.value,
                        entity.discriminator.value,
                        entity.passwordHash,
                        entity.passwordSalt
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKProfileId(id)
        cache?.set(id, entity)
    }

    fun update(entity: RPKProfile) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_PROFILE)
                .set(RPKIT_PROFILE.NAME, entity.name.value)
                .set(RPKIT_PROFILE.DISCRIMINATOR, entity.discriminator.value)
                .set(RPKIT_PROFILE.PASSWORD_HASH, entity.passwordHash)
                .set(RPKIT_PROFILE.PASSWORD_SALT, entity.passwordSalt)
                .where(RPKIT_PROFILE.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
    }

    operator fun get(id: RPKProfileId): RPKProfile? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE.NAME,
                            RPKIT_PROFILE.DISCRIMINATOR,
                            RPKIT_PROFILE.PASSWORD_HASH,
                            RPKIT_PROFILE.PASSWORD_SALT
                    )
                    .from(RPKIT_PROFILE)
                    .where(RPKIT_PROFILE.ID.eq(id.value))
                    .fetchOne() ?: return null
            val profile = RPKProfileImpl(
                    id,
                    RPKProfileName(result.get(RPKIT_PROFILE.NAME)),
                    RPKProfileDiscriminator(result.get(RPKIT_PROFILE.DISCRIMINATOR)),
                    result.get(RPKIT_PROFILE.PASSWORD_HASH),
                    result.get(RPKIT_PROFILE.PASSWORD_SALT)
            )
            cache?.set(id.value, profile)
            return profile
        }
    }

    fun get(name: RPKProfileName, discriminator: RPKProfileDiscriminator): RPKProfile? {
        val result = database.create
                .select(RPKIT_PROFILE.ID)
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name.value))
                .and(RPKIT_PROFILE.DISCRIMINATOR.eq(discriminator.value))
                .fetchOne() ?: return null
        return get(RPKProfileId(result.get(RPKIT_PROFILE.ID)))
    }

    fun delete(entity: RPKProfile) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }

    fun generateDiscriminatorFor(name: RPKProfileName): RPKProfileDiscriminator {
        val result = database.create
                .select(max(RPKIT_PROFILE.DISCRIMINATOR))
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name.value))
                .fetchOne()
        return RPKProfileDiscriminator(result?.get(0, Int::class.javaObjectType)?.plus(1) ?: 1)
    }

}