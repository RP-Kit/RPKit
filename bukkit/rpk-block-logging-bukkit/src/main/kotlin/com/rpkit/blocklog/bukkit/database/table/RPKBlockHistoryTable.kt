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

package com.rpkit.blocklog.bukkit.database.table

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistory
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryId
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryImpl
import com.rpkit.blocklog.bukkit.database.create
import com.rpkit.blocklog.bukkit.database.jooq.Tables.RPKIT_BLOCK_HISTORY
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.bukkit.block.Block
import java.util.concurrent.CompletableFuture


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
            database.create
                .insertInto(
                    RPKIT_BLOCK_HISTORY,
                    RPKIT_BLOCK_HISTORY.WORLD,
                    RPKIT_BLOCK_HISTORY.X,
                    RPKIT_BLOCK_HISTORY.Y,
                    RPKIT_BLOCK_HISTORY.Z
                )
                .values(
                    entity.world.name,
                    entity.x,
                    entity.y,
                    entity.z
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKBlockHistoryId(id)
            cache?.set(id, entity)
        }
    }

    fun update(entity: RPKBlockHistory): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val id = entity.id ?: return@runAsync
            database.create
                .update(RPKIT_BLOCK_HISTORY)
                .set(RPKIT_BLOCK_HISTORY.WORLD, entity.world.name)
                .set(RPKIT_BLOCK_HISTORY.X, entity.x)
                .set(RPKIT_BLOCK_HISTORY.Y, entity.y)
                .set(RPKIT_BLOCK_HISTORY.Z, entity.z)
                .where(RPKIT_BLOCK_HISTORY.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }
    }

    operator fun get(id: RPKBlockHistoryId): CompletableFuture<RPKBlockHistory?> {
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
                    .fetchOne() ?: return@supplyAsync null
                val world = plugin.server.getWorld(result.get(RPKIT_BLOCK_HISTORY.WORLD))
                if (world == null) {
                    database.create
                        .deleteFrom(RPKIT_BLOCK_HISTORY)
                        .where(RPKIT_BLOCK_HISTORY.ID.eq(id.value))
                        .execute()
                    cache?.remove(id.value)
                    return@supplyAsync null
                }
                val blockHistory = RPKBlockHistoryImpl(
                    plugin,
                    id,
                    world,
                    result.get(RPKIT_BLOCK_HISTORY.X),
                    result.get(RPKIT_BLOCK_HISTORY.Y),
                    result.get(RPKIT_BLOCK_HISTORY.Z)
                )
                cache?.set(id.value, blockHistory)
                return@supplyAsync blockHistory
            }
        }
    }

    fun get(block: Block): CompletableFuture<RPKBlockHistory?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_BLOCK_HISTORY.ID)
                .from(RPKIT_BLOCK_HISTORY)
                .where(RPKIT_BLOCK_HISTORY.WORLD.eq(block.world.name))
                .and(RPKIT_BLOCK_HISTORY.X.eq(block.x))
                .and(RPKIT_BLOCK_HISTORY.Y.eq(block.y))
                .and(RPKIT_BLOCK_HISTORY.Z.eq(block.z))
                .fetchOne() ?: return@supplyAsync null
            val id = result.get(RPKIT_BLOCK_HISTORY.ID)
            return@supplyAsync if (id == null) {
                null
            } else {
                get(RPKBlockHistoryId(id)).join()
            }
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
        }
    }

}