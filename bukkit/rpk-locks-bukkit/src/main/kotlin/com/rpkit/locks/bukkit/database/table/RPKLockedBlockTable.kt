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

package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.create
import com.rpkit.locks.bukkit.database.jooq.Tables.RPKIT_LOCKED_BLOCK
import com.rpkit.locks.bukkit.database.jooq.tables.records.RpkitLockedBlockRecord
import com.rpkit.locks.bukkit.lock.RPKLockedBlock
import java.util.concurrent.CompletableFuture


class RPKLockedBlockTable(private val database: Database, private val plugin: RPKLocksBukkit) : Table {

    private data class BlockCacheKey(
         val worldName: String,
         val x: Int,
         val y: Int,
         val z: Int
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_locked_block.block.enabled")) {
        database.cacheManager.createCache("rpk-locks-bukkit.rpkit_locked_block.block",
            BlockCacheKey::class.javaObjectType,
            RPKLockedBlock::class.java,
            plugin.config.getLong("caching.rpkit_locked_block.block.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKLockedBlock): CompletableFuture<Void> {
        val block = entity.block
        val cacheKey = BlockCacheKey(block.world, block.x, block.y, block.z)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_LOCKED_BLOCK,
                    RPKIT_LOCKED_BLOCK.WORLD,
                    RPKIT_LOCKED_BLOCK.X,
                    RPKIT_LOCKED_BLOCK.Y,
                    RPKIT_LOCKED_BLOCK.Z
                )
                .values(
                    block.world,
                    block.x,
                    block.y,
                    block.z
                )
                .execute()
            cache?.set(cacheKey, entity)
        }
    }

    operator fun get(block: RPKBlockLocation): CompletableFuture<RPKLockedBlock?> {
        val cacheKey = BlockCacheKey(block.world, block.x, block.y, block.z)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            database.create
                .select(
                    RPKIT_LOCKED_BLOCK.WORLD,
                    RPKIT_LOCKED_BLOCK.X,
                    RPKIT_LOCKED_BLOCK.Y,
                    RPKIT_LOCKED_BLOCK.Z
                )
                .from(RPKIT_LOCKED_BLOCK)
                .where(RPKIT_LOCKED_BLOCK.WORLD.eq(block.world))
                .and(RPKIT_LOCKED_BLOCK.X.eq(block.x))
                .and(RPKIT_LOCKED_BLOCK.Y.eq(block.y))
                .and(RPKIT_LOCKED_BLOCK.Z.eq(block.z))
                .fetchOne() ?: return@supplyAsync null
            val lockedBlock = RPKLockedBlock(block)
            cache?.set(cacheKey, lockedBlock)
            return@supplyAsync lockedBlock
        }
    }

    fun getAll(): CompletableFuture<List<RPKLockedBlock>> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync database.create
                .selectFrom(RPKIT_LOCKED_BLOCK)
                .fetch()
                .mapNotNull { it.toDomain() }
        }
    }

    private fun RpkitLockedBlockRecord.toDomain(): RPKLockedBlock {
        return RPKLockedBlock(
            block = RPKBlockLocation(world, x, y, z)
        )
    }

    fun delete(entity: RPKLockedBlock): CompletableFuture<Void> {
        val block = entity.block
        val cacheKey = BlockCacheKey(block.world, block.x, block.y, block.z)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_LOCKED_BLOCK)
                .where(RPKIT_LOCKED_BLOCK.WORLD.eq(block.world))
                .and(RPKIT_LOCKED_BLOCK.X.eq(block.x))
                .and(RPKIT_LOCKED_BLOCK.Y.eq(block.y))
                .and(RPKIT_LOCKED_BLOCK.Z.eq(block.z))
                .execute()
            cache?.remove(cacheKey)
        }
    }

}