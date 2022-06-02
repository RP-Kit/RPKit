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
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_PROFILE
import com.rpkit.players.bukkit.profile.*
import org.jooq.impl.DSL.max
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


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

    fun insert(entity: RPKProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
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
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert profile", exception)
            throw exception
        }
    }

    fun update(entity: RPKProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_PROFILE)
                .set(RPKIT_PROFILE.NAME, entity.name.value)
                .set(RPKIT_PROFILE.DISCRIMINATOR, entity.discriminator.value)
                .set(RPKIT_PROFILE.PASSWORD_HASH, entity.passwordHash)
                .set(RPKIT_PROFILE.PASSWORD_SALT, entity.passwordSalt)
                .where(RPKIT_PROFILE.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update profile", exception)
            throw exception
        }
    }

    operator fun get(id: RPKProfileId): CompletableFuture<out RPKProfile?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_PROFILE.NAME,
                        RPKIT_PROFILE.DISCRIMINATOR,
                        RPKIT_PROFILE.PASSWORD_HASH,
                        RPKIT_PROFILE.PASSWORD_SALT
                    )
                    .from(RPKIT_PROFILE)
                    .where(RPKIT_PROFILE.ID.eq(id.value))
                    .fetchOne() ?: return@supplyAsync null
                val profile = RPKProfileImpl(
                    id,
                    RPKProfileName(result.get(RPKIT_PROFILE.NAME)),
                    RPKProfileDiscriminator(result.get(RPKIT_PROFILE.DISCRIMINATOR)),
                    result.get(RPKIT_PROFILE.PASSWORD_HASH),
                    result.get(RPKIT_PROFILE.PASSWORD_SALT)
                )
                cache?.set(id.value, profile)
                return@supplyAsync profile
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get profile", exception)
                throw exception
            }
        }
    }

    fun get(name: RPKProfileName, discriminator: RPKProfileDiscriminator): CompletableFuture<RPKProfile?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_PROFILE.ID)
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name.value))
                .and(RPKIT_PROFILE.DISCRIMINATOR.eq(discriminator.value))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync get(RPKProfileId(result.get(RPKIT_PROFILE.ID))).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get profile", exception)
            throw exception
        }
    }

    fun delete(entity: RPKProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete profile", exception)
            throw exception
        }
    }

    fun generateDiscriminatorFor(name: RPKProfileName): CompletableFuture<RPKProfileDiscriminator> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(max(RPKIT_PROFILE.DISCRIMINATOR))
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name.value))
                .fetchOne()
            return@supplyAsync RPKProfileDiscriminator(result?.get(0, Int::class.javaObjectType)?.plus(1) ?: 1)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to generate discriminator for profile", exception)
            throw exception
        }
    }

}