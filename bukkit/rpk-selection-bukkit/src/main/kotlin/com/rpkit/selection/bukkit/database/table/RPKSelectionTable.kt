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

package com.rpkit.selection.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.database.create
import com.rpkit.selection.bukkit.database.jooq.Tables.RPKIT_SELECTION_
import com.rpkit.selection.bukkit.selection.RPKSelection
import com.rpkit.selection.bukkit.selection.RPKSelectionImpl
import java.util.concurrent.CompletableFuture


class RPKSelectionTable(private val database: Database, private val plugin: RPKSelectionBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_selection.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-selection-bukkit.rpkit_selection.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKSelection::class.java,
            plugin.config.getLong("caching.rpkit_selection.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKSelection): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create.insertInto(
                RPKIT_SELECTION_,
                RPKIT_SELECTION_.MINECRAFT_PROFILE_ID,
                RPKIT_SELECTION_.WORLD,
                RPKIT_SELECTION_.X_1,
                RPKIT_SELECTION_.Y_1,
                RPKIT_SELECTION_.Z_1,
                RPKIT_SELECTION_.X_2,
                RPKIT_SELECTION_.Y_2,
                RPKIT_SELECTION_.Z_2
            ).values(
                minecraftProfileId.value,
                entity.world,
                entity.minimumPoint.x,
                entity.minimumPoint.y,
                entity.minimumPoint.z,
                entity.maximumPoint.x,
                entity.maximumPoint.y,
                entity.maximumPoint.z
            )
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }
    }

    fun update(entity: RPKSelection): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_SELECTION_)
                .set(RPKIT_SELECTION_.WORLD, entity.world)
                .set(RPKIT_SELECTION_.X_1, entity.minimumPoint.x)
                .set(RPKIT_SELECTION_.Y_1, entity.minimumPoint.y)
                .set(RPKIT_SELECTION_.Z_1, entity.minimumPoint.z)
                .set(RPKIT_SELECTION_.X_2, entity.maximumPoint.x)
                .set(RPKIT_SELECTION_.Y_2, entity.maximumPoint.y)
                .set(RPKIT_SELECTION_.Z_2, entity.maximumPoint.z)
                .where(RPKIT_SELECTION_.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKSelection?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(cache[minecraftProfileId.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create.select(
                    RPKIT_SELECTION_.MINECRAFT_PROFILE_ID,
                    RPKIT_SELECTION_.WORLD,
                    RPKIT_SELECTION_.X_1,
                    RPKIT_SELECTION_.Y_1,
                    RPKIT_SELECTION_.Z_1,
                    RPKIT_SELECTION_.X_2,
                    RPKIT_SELECTION_.Y_2,
                    RPKIT_SELECTION_.Z_2
                )
                    .from(RPKIT_SELECTION_)
                    .where(RPKIT_SELECTION_.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .fetchOne() ?: return@supplyAsync null
                val block1 = RPKBlockLocation(
                    result[RPKIT_SELECTION_.WORLD],
                    result[RPKIT_SELECTION_.X_1],
                    result[RPKIT_SELECTION_.Y_1],
                    result[RPKIT_SELECTION_.Z_1]
                )
                val block2 = RPKBlockLocation(
                    result[RPKIT_SELECTION_.WORLD],
                    result[RPKIT_SELECTION_.X_2],
                    result[RPKIT_SELECTION_.Y_2],
                    result[RPKIT_SELECTION_.Z_2]
                )
                val selection = RPKSelectionImpl(
                    minecraftProfile,
                    result[RPKIT_SELECTION_.WORLD],
                    block1,
                    block2
                )
                cache?.set(minecraftProfileId.value, selection)
                return@supplyAsync selection
            }
        }
    }

    fun delete(entity: RPKSelection): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_SELECTION_)
                .where(RPKIT_SELECTION_.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.remove(minecraftProfileId.value)
        }
    }

}