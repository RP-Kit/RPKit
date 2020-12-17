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

    fun insert(entity: RPKStoreItem): Int {
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
        entity.id = id
        cache?.set(id, entity)
        return id
    }

    fun update(entity: RPKStoreItem) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_STORE_ITEM)
                .set(RPKIT_STORE_ITEM.PLUGIN, entity.plugin)
                .set(RPKIT_STORE_ITEM.IDENTIFIER, entity.identifier)
                .set(RPKIT_STORE_ITEM.DESCRIPTION, entity.description)
                .set(RPKIT_STORE_ITEM.COST, entity.cost)
                .where(RPKIT_STORE_ITEM.ID.eq(id))
                .execute()
        cache?.set(id, entity)
    }

    operator fun get(id: Int): RPKStoreItem? {
        if (cache?.containsKey(id) == true) return cache[id]
        var storeItem: RPKStoreItem? = database.getTable(RPKConsumableStoreItemTable::class.java)[id]
        if (storeItem != null) {
            cache?.set(id, storeItem)
            return storeItem
        } else {
            cache?.remove(id)
        }
        storeItem = database.getTable(RPKPermanentStoreItemTable::class.java)[id]
        if (storeItem != null) {
            cache?.set(id, storeItem)
            return storeItem
        } else {
            cache?.remove(id)
        }
        storeItem = database.getTable(RPKTimedStoreItemTable::class.java)[id]
        if (storeItem != null) {
            cache?.set(id, storeItem)
            return storeItem
        } else {
            cache?.remove(id)
        }
        return null
    }

    fun get(plugin: RPKBukkitPlugin, identifier: String): RPKStoreItem? {
        val result = database.create
                .select(RPKIT_STORE_ITEM.ID)
                .from(RPKIT_STORE_ITEM)
                .where(RPKIT_STORE_ITEM.PLUGIN.eq(plugin.name))
                .and(RPKIT_STORE_ITEM.IDENTIFIER.eq(identifier))
                .fetchOne()
        return get(result[RPKIT_STORE_ITEM.ID])
    }

    fun getAll(): List<RPKStoreItem> {
        val result = database.create
                .select(RPKIT_STORE_ITEM.ID)
                .from(RPKIT_STORE_ITEM)
                .fetch()
        return result.mapNotNull { row -> get(row[RPKIT_STORE_ITEM.ID]) }
    }

    fun delete(entity: RPKStoreItem) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_STORE_ITEM)
                .where(RPKIT_STORE_ITEM.ID.eq(id))
                .execute()
        cache?.remove(id)
    }
}