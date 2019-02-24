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

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.storeitem.RPKConsumableStoreItem
import com.rpkit.store.bukkit.storeitem.RPKConsumableStoreItemImpl
import com.rpkit.stores.bukkit.database.jooq.rpkit.Tables.RPKIT_CONSUMABLE_STORE_ITEM
import com.rpkit.stores.bukkit.database.jooq.rpkit.Tables.RPKIT_STORE_ITEM
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKConsumableStoreItemTable(database: Database, private val plugin: RPKStoresBukkit): Table<RPKConsumableStoreItem>(database, RPKConsumableStoreItem::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_consumable_store_item.id.enabled")) {
        database.cacheManager.createCache("rpkit-stores-bukkit.rpkit_consumable_store_item.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKConsumableStoreItem::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_consumable_store_item.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CONSUMABLE_STORE_ITEM)
                .column(RPKIT_CONSUMABLE_STORE_ITEM.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CONSUMABLE_STORE_ITEM.STORE_ITEM_ID, SQLDataType.INTEGER)
                .column(RPKIT_CONSUMABLE_STORE_ITEM.USES, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_consumable_store_item").primaryKey(RPKIT_CONSUMABLE_STORE_ITEM.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.6.0")
        }
    }

    override fun insert(entity: RPKConsumableStoreItem): Int {
        val id = database.getTable(RPKStoreItemTable::class).insert(entity)
        database.create
                .insertInto(
                        RPKIT_CONSUMABLE_STORE_ITEM,
                        RPKIT_CONSUMABLE_STORE_ITEM.STORE_ITEM_ID,
                        RPKIT_CONSUMABLE_STORE_ITEM.USES
                )
                .values(
                        id,
                        entity.uses
                )
                .execute()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKConsumableStoreItem) {
        database.getTable(RPKStoreItemTable::class).update(entity)
        database.create
                .update(RPKIT_CONSUMABLE_STORE_ITEM)
                .set(RPKIT_CONSUMABLE_STORE_ITEM.USES, entity.uses)
                .where(RPKIT_CONSUMABLE_STORE_ITEM.STORE_ITEM_ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKConsumableStoreItem? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_STORE_ITEM.PLUGIN,
                            RPKIT_STORE_ITEM.IDENTIFIER,
                            RPKIT_STORE_ITEM.DESCRIPTION,
                            RPKIT_STORE_ITEM.COST,
                            RPKIT_CONSUMABLE_STORE_ITEM.USES
                    )
                    .from(
                            RPKIT_STORE_ITEM,
                            RPKIT_CONSUMABLE_STORE_ITEM
                    )
                    .where(RPKIT_STORE_ITEM.ID.eq(id))
                    .and(RPKIT_STORE_ITEM.ID.eq(RPKIT_CONSUMABLE_STORE_ITEM.STORE_ITEM_ID))
                    .fetchOne() ?: return null
            val storeItem = RPKConsumableStoreItemImpl(
                    id,
                    result[RPKIT_CONSUMABLE_STORE_ITEM.USES],
                    result[RPKIT_STORE_ITEM.PLUGIN],
                    result[RPKIT_STORE_ITEM.IDENTIFIER],
                    result[RPKIT_STORE_ITEM.DESCRIPTION],
                    result[RPKIT_STORE_ITEM.COST]
            )
            cache?.put(id, storeItem)
            return storeItem
        }
    }

    override fun delete(entity: RPKConsumableStoreItem) {
        database.getTable(RPKStoreItemTable::class).delete(entity)
        database.create
                .deleteFrom(RPKIT_CONSUMABLE_STORE_ITEM)
                .where(RPKIT_CONSUMABLE_STORE_ITEM.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}