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

package com.rpkit.store.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_STORE_ITEM
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_TIMED_STORE_ITEM
import com.rpkit.store.bukkit.storeitem.RPKStoreItemId
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItem
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItemImpl
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKTimedStoreItemTable(
        private val database: Database,
        private val plugin: RPKStoresBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_timed_store_item.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_timed_store_item.id",
            Int::class.javaObjectType,
            RPKTimedStoreItem::class.java,
            plugin.config.getLong("caching.rpkit_consumable_purchase.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKTimedStoreItem): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val id = database.getTable(RPKStoreItemTable::class.java).insert(entity).join()
            database.create
                .insertInto(
                    RPKIT_TIMED_STORE_ITEM,
                    RPKIT_TIMED_STORE_ITEM.STORE_ITEM_ID,
                    RPKIT_TIMED_STORE_ITEM.DURATION
                )
                .values(
                    id.value,
                    entity.duration.seconds
                )
                .execute()
            entity.id = id
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert timed store item", exception)
            throw exception
        }
    }

    fun update(entity: RPKTimedStoreItem): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKStoreItemTable::class.java).update(entity).join()
            database.create
                .update(RPKIT_TIMED_STORE_ITEM)
                .set(RPKIT_TIMED_STORE_ITEM.DURATION, entity.duration.seconds)
                .where(RPKIT_TIMED_STORE_ITEM.STORE_ITEM_ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update timed store item", exception)
            throw exception
        }
    }

    operator fun get(id: RPKStoreItemId): CompletableFuture<out RPKTimedStoreItem?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_STORE_ITEM.PLUGIN,
                        RPKIT_STORE_ITEM.IDENTIFIER,
                        RPKIT_STORE_ITEM.DESCRIPTION,
                        RPKIT_STORE_ITEM.COST,
                        RPKIT_TIMED_STORE_ITEM.DURATION
                    )
                    .from(
                        RPKIT_STORE_ITEM,
                        RPKIT_TIMED_STORE_ITEM
                    )
                    .where(RPKIT_STORE_ITEM.ID.eq(id.value))
                    .and(RPKIT_STORE_ITEM.ID.eq(RPKIT_TIMED_STORE_ITEM.ID))
                    .fetchOne() ?: return@supplyAsync null
                val storeItem = RPKTimedStoreItemImpl(
                    id,
                    Duration.ofSeconds(result[RPKIT_TIMED_STORE_ITEM.DURATION]),
                    result[RPKIT_STORE_ITEM.PLUGIN],
                    result[RPKIT_STORE_ITEM.IDENTIFIER],
                    result[RPKIT_STORE_ITEM.DESCRIPTION],
                    result[RPKIT_STORE_ITEM.COST]
                )
                cache?.set(id.value, storeItem)
                return@supplyAsync storeItem
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get timed store item", exception)
                throw exception
            }
        }
    }

    fun delete(entity: RPKTimedStoreItem): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKStoreItemTable::class.java).delete(entity).join()
            database.create
                .deleteFrom(RPKIT_TIMED_STORE_ITEM)
                .where(RPKIT_TIMED_STORE_ITEM.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete timed store item", exception)
            throw exception
        }
    }

}