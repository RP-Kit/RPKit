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

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PERMANENT_STORE_ITEM
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_STORE_ITEM
import com.rpkit.store.bukkit.storeitem.RPKPermanentStoreItem
import com.rpkit.store.bukkit.storeitem.RPKPermanentStoreItemImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemId
import java.util.concurrent.CompletableFuture


class RPKPermanentStoreItemTable(
        private val database: Database,
        plugin: RPKStoresBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_permanent_store_item.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_permanent_store_item.id",
            Int::class.javaObjectType,
            RPKPermanentStoreItem::class.java,
            plugin.config.getLong("caching.rpkit_permanent_store_item.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPermanentStoreItem): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val id = database.getTable(RPKStoreItemTable::class.java).insert(entity).join()
            database.create
                .insertInto(
                    RPKIT_PERMANENT_STORE_ITEM,
                    RPKIT_PERMANENT_STORE_ITEM.STORE_ITEM_ID
                )
                .values(
                    id.value
                )
                .execute()
            entity.id = id
            cache?.set(id.value, entity)
        }
    }

    fun update(entity: RPKPermanentStoreItem): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKStoreItemTable::class.java).update(entity).join()
            cache?.set(id.value, entity)
        }
    }

    operator fun get(id: RPKStoreItemId): CompletableFuture<RPKPermanentStoreItem?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_STORE_ITEM.PLUGIN,
                        RPKIT_STORE_ITEM.IDENTIFIER,
                        RPKIT_STORE_ITEM.DESCRIPTION,
                        RPKIT_STORE_ITEM.COST
                    )
                    .from(
                        RPKIT_STORE_ITEM,
                        RPKIT_PERMANENT_STORE_ITEM
                    )
                    .where(RPKIT_STORE_ITEM.ID.eq(id.value))
                    .and(RPKIT_STORE_ITEM.ID.eq(RPKIT_PERMANENT_STORE_ITEM.STORE_ITEM_ID))
                    .fetchOne() ?: return@supplyAsync null
                val storeItem = RPKPermanentStoreItemImpl(
                    id,
                    result[RPKIT_STORE_ITEM.PLUGIN],
                    result[RPKIT_STORE_ITEM.IDENTIFIER],
                    result[RPKIT_STORE_ITEM.DESCRIPTION],
                    result[RPKIT_STORE_ITEM.COST]
                )
                cache?.set(id.value, storeItem)
                return@supplyAsync storeItem
            }
        }
    }

    fun delete(entity: RPKPermanentStoreItem): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKStoreItemTable::class.java).delete(entity).join()
            database.create
                .deleteFrom(RPKIT_PERMANENT_STORE_ITEM)
                .where(RPKIT_PERMANENT_STORE_ITEM.STORE_ITEM_ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }
    }

}