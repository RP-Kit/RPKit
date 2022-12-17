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

package com.rpkit.blocklog.bukkit.database.table

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistory
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryId
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryImpl
import com.rpkit.blocklog.bukkit.database.create
import com.rpkit.blocklog.bukkit.database.jooq.Tables.RPKIT_BLOCK_HISTORY
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKBlockLocation
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.`val`
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKBlockHistoryTable(private val database: Database, private val plugin: RPKBlockLoggingBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_block_history.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-block-logging-bukkit.rpkit_block_history.id",
            Int::class.javaObjectType,
            RPKBlockHistory::class.java,
            plugin.config.getLong("caching.rpkit_block_history.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKBlockHistory): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val rowCount = database.create
                .insertInto(
                    RPKIT_BLOCK_HISTORY,
                    RPKIT_BLOCK_HISTORY.WORLD,
                    RPKIT_BLOCK_HISTORY.X,
                    RPKIT_BLOCK_HISTORY.Y,
                    RPKIT_BLOCK_HISTORY.Z
                )
                .select(
                    select(
                        `val`(entity.world),
                        `val`(entity.x),
                        `val`(entity.y),
                        `val`(entity.z)
                    ).whereNotExists(
                        database.create.selectFrom(RPKIT_BLOCK_HISTORY)
                            .where(RPKIT_BLOCK_HISTORY.WORLD.eq(entity.world))
                            .and(RPKIT_BLOCK_HISTORY.X.eq(entity.x))
                            .and(RPKIT_BLOCK_HISTORY.Y.eq(entity.y))
                            .and(RPKIT_BLOCK_HISTORY.Z.eq(entity.z))
                    )
                )
                .execute()
            if (rowCount == 0) {
                val existingBlockHistory = get(RPKBlockLocation(entity.world, entity.x, entity.y, entity.z)).join()
                if (existingBlockHistory != null) {
                    val existingBlockHistoryId = existingBlockHistory.id
                    entity.id = existingBlockHistoryId
                }
                return@runAsync
            }
            val id = database.create.lastID().toInt()
            entity.id = RPKBlockHistoryId(id)
            cache?.set(id, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert block history", exception)
            throw exception
        }
    }

    fun update(entity: RPKBlockHistory): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val id = entity.id ?: return@runAsync
            database.create
                .update(RPKIT_BLOCK_HISTORY)
                .set(RPKIT_BLOCK_HISTORY.WORLD, entity.world)
                .set(RPKIT_BLOCK_HISTORY.X, entity.x)
                .set(RPKIT_BLOCK_HISTORY.Y, entity.y)
                .set(RPKIT_BLOCK_HISTORY.Z, entity.z)
                .where(RPKIT_BLOCK_HISTORY.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update block history", exception)
            throw exception
        }
    }

    operator fun get(id: RPKBlockHistoryId): CompletableFuture<out RPKBlockHistory?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_BLOCK_HISTORY.WORLD,
                        RPKIT_BLOCK_HISTORY.X,
                        RPKIT_BLOCK_HISTORY.Y,
                        RPKIT_BLOCK_HISTORY.Z
                    )
                    .from(RPKIT_BLOCK_HISTORY)
                    .where(RPKIT_BLOCK_HISTORY.ID.eq(id.value))
                    .orderBy(RPKIT_BLOCK_HISTORY.ID)
                    .limit(1)
                    .fetchOne() ?: return@supplyAsync null
                val blockHistory = RPKBlockHistoryImpl(
                    plugin,
                    id,
                    result[RPKIT_BLOCK_HISTORY.WORLD],
                    result[RPKIT_BLOCK_HISTORY.X],
                    result[RPKIT_BLOCK_HISTORY.Y],
                    result[RPKIT_BLOCK_HISTORY.Z]
                )
                cache?.set(id.value, blockHistory)
                return@supplyAsync blockHistory
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get block history", exception)
                throw exception
            }
        }
    }

    fun get(block: RPKBlockLocation): CompletableFuture<RPKBlockHistory?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_BLOCK_HISTORY.ID)
                .from(RPKIT_BLOCK_HISTORY)
                .where(RPKIT_BLOCK_HISTORY.WORLD.eq(block.world))
                .and(RPKIT_BLOCK_HISTORY.X.eq(block.x))
                .and(RPKIT_BLOCK_HISTORY.Y.eq(block.y))
                .and(RPKIT_BLOCK_HISTORY.Z.eq(block.z))
                .orderBy(RPKIT_BLOCK_HISTORY.ID)
                .limit(1)
                .fetchOne() ?: return@supplyAsync null
            val id = result.get(RPKIT_BLOCK_HISTORY.ID)
            return@supplyAsync if (id == null) {
                null
            } else {
                get(RPKBlockHistoryId(id)).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get block history", exception)
            throw exception
        }
    }

    fun delete(entity: RPKBlockHistory): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_BLOCK_HISTORY)
                .where(RPKIT_BLOCK_HISTORY.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete block history", exception)
            throw exception
        }
    }

}