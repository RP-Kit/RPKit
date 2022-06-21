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

package com.rpkit.essentials.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.create
import com.rpkit.essentials.bukkit.database.jooq.Tables.RPKIT_TRACKING_DISABLED
import com.rpkit.essentials.bukkit.tracking.RPKTrackingDisabled
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level.SEVERE


class RPKTrackingDisabledTable(private val database: Database, private val plugin: RPKEssentialsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_tracking_enabled.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-essentials-bukkit.rpkit_tracking_enabled.character_id",
            Int::class.javaObjectType,
            RPKTrackingDisabled::class.java,
            plugin.config.getLong("caching.rpkit_tracking_enabled.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKTrackingDisabled): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_TRACKING_DISABLED,
                    RPKIT_TRACKING_DISABLED.CHARACTER_ID
                )
                .values(
                    characterId.value
                )
                .execute()
            cache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to insert tracking disabled", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter): CompletableFuture<RPKTrackingDisabled?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(characterId.value) == true) {
            return CompletableFuture.completedFuture(cache[characterId.value])
        }
        return CompletableFuture.supplyAsync {
            database.create
                .select(
                    RPKIT_TRACKING_DISABLED.CHARACTER_ID
                )
                .from(RPKIT_TRACKING_DISABLED)
                .where(RPKIT_TRACKING_DISABLED.CHARACTER_ID.eq(characterId.value))
                .fetchOne() ?: return@supplyAsync null
            val trackingEnabled = RPKTrackingDisabled(character)
            cache?.set(characterId.value, trackingEnabled)
            return@supplyAsync trackingEnabled
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to get tracking disabled", exception)
            throw exception
        }
    }

    fun delete(entity: RPKTrackingDisabled): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_TRACKING_DISABLED)
                .where(RPKIT_TRACKING_DISABLED.CHARACTER_ID.eq(characterId.value))
                .execute()
            cache?.remove(characterId.value)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to delete tracking disabled", exception)
            throw exception
        }
    }

    fun delete(characterId: RPKCharacterId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_TRACKING_DISABLED)
            .where(RPKIT_TRACKING_DISABLED.CHARACTER_ID.eq(characterId.value))
            .execute()
        cache?.remove(characterId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete tracking disabled for character id", exception)
        throw exception
    }
}