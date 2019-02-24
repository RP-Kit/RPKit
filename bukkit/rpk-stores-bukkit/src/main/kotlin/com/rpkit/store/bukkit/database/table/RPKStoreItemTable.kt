/*
 * Copyright 2018 Ross Binden
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
import com.rpkit.store.bukkit.storeitem.RPKStoreItem
import com.rpkit.stores.bukkit.database.jooq.rpkit.Tables.RPKIT_STORE_ITEM
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKStoreItemTable(database: Database, private val plugin: RPKStoresBukkit): Table<RPKStoreItem>(database, RPKStoreItem::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_store_item.id.enabled")) {
        database.cacheManager.createCache("rpkit-stores-bukkit.rpkit_store_item.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKStoreItem::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_store_item.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_STORE_ITEM)
                .column(RPKIT_STORE_ITEM.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_STORE_ITEM.PLUGIN, SQLDataType.VARCHAR(128))
                .column(RPKIT_STORE_ITEM.IDENTIFIER, SQLDataType.VARCHAR(128))
                .column(RPKIT_STORE_ITEM.DESCRIPTION, SQLDataType.VARCHAR(2048))
                .column(RPKIT_STORE_ITEM.COST, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_store_item").primaryKey(RPKIT_STORE_ITEM.ID),
                        constraint("uk_rpkit_store_item").unique(RPKIT_STORE_ITEM.PLUGIN, RPKIT_STORE_ITEM.IDENTIFIER)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.6.0")
        }
    }

    override fun insert(entity: RPKStoreItem): Int {
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
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKStoreItem) {
        database.create
                .update(RPKIT_STORE_ITEM)
                .set(RPKIT_STORE_ITEM.PLUGIN, entity.plugin)
                .set(RPKIT_STORE_ITEM.IDENTIFIER, entity.identifier)
                .set(RPKIT_STORE_ITEM.DESCRIPTION, entity.description)
                .set(RPKIT_STORE_ITEM.COST, entity.cost)
                .where(RPKIT_STORE_ITEM.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKStoreItem? {
        if (cache?.containsKey(id) == true) return cache[id]
        var storeItem: RPKStoreItem? = database.getTable(RPKConsumableStoreItemTable::class)[id]
        if (storeItem != null) {
            cache?.put(id, storeItem)
            return storeItem
        } else {
            cache?.remove(id)
        }
        storeItem = database.getTable(RPKPermanentStoreItemTable::class)[id]
        if (storeItem != null) {
            cache?.put(id, storeItem)
            return storeItem
        } else {
            cache?.remove(id)
        }
        storeItem = database.getTable(RPKTimedStoreItemTable::class)[id]
        if (storeItem != null) {
            cache?.put(id, storeItem)
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

    override fun delete(entity: RPKStoreItem) {
        database.create
                .deleteFrom(RPKIT_STORE_ITEM)
                .where(RPKIT_STORE_ITEM.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}