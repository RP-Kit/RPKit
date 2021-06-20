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

package com.rpkit.store.bukkit.database.table

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_STORE_ITEM
import com.rpkit.store.bukkit.storeitem.RPKStoreItem
import com.rpkit.store.bukkit.storeitem.RPKStoreItemId
import java.util.concurrent.CompletableFuture


class RPKStoreItemTable(
        private val database: Database,
        plugin: RPKStoresBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_store_item.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_store_item.id",
            Int::class.javaObjectType,
            RPKStoreItem::class.java,
            plugin.config.getLong("caching.rpkit_store_item.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKStoreItem): CompletableFuture<RPKStoreItemId> {
        return CompletableFuture.supplyAsync {
            database.create
                .insertInto(
                    RPKIT_STORE_ITEM,
                    RPKIT_STORE_ITEM.PLUGIN,
                    RPKIT_STORE_ITEM.IDENTIFIER,
                    RPKIT_STORE_ITEM.DESCRIPTION,
                    RPKIT_STORE_ITEM.COST
                )
                .values(
                    entity.plugin,
                    entity.identifier,
                    entity.description,
                    entity.cost
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKStoreItemId(id)
            cache?.set(id, entity)
            return@supplyAsync RPKStoreItemId(id)
        }
    }

    fun update(entity: RPKStoreItem): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_STORE_ITEM)
                .set(RPKIT_STORE_ITEM.PLUGIN, entity.plugin)
                .set(RPKIT_STORE_ITEM.IDENTIFIER, entity.identifier)
                .set(RPKIT_STORE_ITEM.DESCRIPTION, entity.description)
                .set(RPKIT_STORE_ITEM.COST, entity.cost)
                .where(RPKIT_STORE_ITEM.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }
    }

    operator fun get(id: RPKStoreItemId): CompletableFuture<RPKStoreItem?> {
        if (cache?.containsKey(id.value) == true) return CompletableFuture.completedFuture(cache[id.value])
        return CompletableFuture.supplyAsync {
            var storeItem: RPKStoreItem? = database.getTable(RPKConsumableStoreItemTable::class.java)[id].join()
            if (storeItem != null) {
                cache?.set(id.value, storeItem)
                return@supplyAsync storeItem
            } else {
                cache?.remove(id.value)
            }
            storeItem = database.getTable(RPKPermanentStoreItemTable::class.java)[id].join()
            if (storeItem != null) {
                cache?.set(id.value, storeItem)
                return@supplyAsync storeItem
            } else {
                cache?.remove(id.value)
            }
            storeItem = database.getTable(RPKTimedStoreItemTable::class.java)[id].join()
            if (storeItem != null) {
                cache?.set(id.value, storeItem)
                return@supplyAsync storeItem
            } else {
                cache?.remove(id.value)
            }
            return@supplyAsync null
        }
    }

    fun get(plugin: RPKBukkitPlugin, identifier: String): CompletableFuture<RPKStoreItem?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_STORE_ITEM.ID)
                .from(RPKIT_STORE_ITEM)
                .where(RPKIT_STORE_ITEM.PLUGIN.eq(plugin.name))
                .and(RPKIT_STORE_ITEM.IDENTIFIER.eq(identifier))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync get(RPKStoreItemId(result[RPKIT_STORE_ITEM.ID])).join()
        }
    }

    fun getAll(): CompletableFuture<List<RPKStoreItem>> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_STORE_ITEM.ID)
                .from(RPKIT_STORE_ITEM)
                .fetch()
            val storeItemFutures = result.map { row -> get(RPKStoreItemId(row[RPKIT_STORE_ITEM.ID])) }
            CompletableFuture.allOf(*storeItemFutures.toTypedArray()).join()
            return@supplyAsync storeItemFutures.mapNotNull(CompletableFuture<RPKStoreItem?>::join)
        }
    }

    fun delete(entity: RPKStoreItem): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_STORE_ITEM)
                .where(RPKIT_STORE_ITEM.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }
    }
}