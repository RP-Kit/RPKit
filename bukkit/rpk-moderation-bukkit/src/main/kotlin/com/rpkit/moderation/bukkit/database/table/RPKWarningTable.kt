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

package com.rpkit.moderation.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.create
import com.rpkit.moderation.bukkit.database.jooq.Tables.RPKIT_WARNING
import com.rpkit.moderation.bukkit.warning.RPKWarning
import com.rpkit.moderation.bukkit.warning.RPKWarningId
import com.rpkit.moderation.bukkit.warning.RPKWarningImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level


class RPKWarningTable(private val database: Database, private val plugin: RPKModerationBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_warning.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-moderation-bukkit.rpkit_warning.id",
            Int::class.javaObjectType,
            RPKWarning::class.java,
            plugin.config.getLong("caching.rpkit_warning.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKWarning): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val issuerId = entity.issuer.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_WARNING,
                    RPKIT_WARNING.REASON,
                    RPKIT_WARNING.PROFILE_ID,
                    RPKIT_WARNING.ISSUER_ID,
                    RPKIT_WARNING.TIME
                )
                .values(
                    entity.reason,
                    profileId.value,
                    issuerId.value,
                    entity.time
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKWarningId(id)
            cache?.set(id, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert warning", exception)
            throw exception
        }
    }

    fun update(entity: RPKWarning): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val issuerId = entity.issuer.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .update(RPKIT_WARNING)
                .set(RPKIT_WARNING.REASON, entity.reason)
                .set(RPKIT_WARNING.PROFILE_ID, profileId.value)
                .set(RPKIT_WARNING.ISSUER_ID, issuerId.value)
                .set(RPKIT_WARNING.TIME, entity.time)
                .where(RPKIT_WARNING.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update warning", exception)
            throw exception
        }
    }

    operator fun get(id: RPKWarningId): CompletableFuture<out RPKWarning?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_WARNING.REASON,
                    RPKIT_WARNING.PROFILE_ID,
                    RPKIT_WARNING.ISSUER_ID,
                    RPKIT_WARNING.TIME
                )
                .from(RPKIT_WARNING)
                .where(RPKIT_WARNING.ID.eq(id.value))
                .fetchOne() ?: return@supplyAsync null
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync null
            val profile = profileService.getProfile(RPKProfileId(result[RPKIT_WARNING.PROFILE_ID])).join()
            val issuer = profileService.getProfile(RPKProfileId(result[RPKIT_WARNING.ISSUER_ID])).join()
            if (profile != null && issuer != null) {
                val warning = RPKWarningImpl(
                    id,
                    result[RPKIT_WARNING.REASON],
                    profile,
                    issuer,
                    result[RPKIT_WARNING.TIME]
                )
                cache?.set(id.value, warning)
                return@supplyAsync warning
            } else {
                database.create
                    .deleteFrom(RPKIT_WARNING)
                    .where(RPKIT_WARNING.ID.eq(id.value))
                    .execute()
                cache?.remove(id.value)
                return@supplyAsync null
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update warning", exception)
            throw exception
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKWarning>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_WARNING.ID)
                .from(RPKIT_WARNING)
                .where(RPKIT_WARNING.PROFILE_ID.eq(profileId.value))
                .fetch()
            val warningFutures = results.map { get(RPKWarningId(it[RPKIT_WARNING.ID])) }
            CompletableFuture.allOf(*warningFutures.toTypedArray()).join()
            return@supplyAsync warningFutures.mapNotNull(CompletableFuture<out RPKWarning?>::join)
        }
    }

    fun delete(entity: RPKWarning): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_WARNING)
                .where(RPKIT_WARNING.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }
    }

    fun delete(profileId: RPKProfileId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_WARNING)
            .where(RPKIT_WARNING.PROFILE_ID.eq(profileId.value))
            .execute()
        cache?.removeMatching { it.profile.id?.value == profileId.value }
    }
}