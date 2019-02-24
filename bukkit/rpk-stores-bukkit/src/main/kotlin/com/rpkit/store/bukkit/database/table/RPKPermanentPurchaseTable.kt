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
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.purchase.RPKPermanentPurchase
import com.rpkit.store.bukkit.purchase.RPKPermanentPurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKPermanentStoreItem
import com.rpkit.store.bukkit.storeitem.RPKStoreItemProvider
import com.rpkit.stores.bukkit.database.jooq.rpkit.Tables.RPKIT_PERMANENT_PURCHASE
import com.rpkit.stores.bukkit.database.jooq.rpkit.Tables.RPKIT_PURCHASE
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType


class RPKPermanentPurchaseTable(database: Database, private val plugin: RPKStoresBukkit): Table<RPKPermanentPurchase>(database, RPKPermanentPurchase::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_permanent_purchase.id.enabled")) {
        database.cacheManager.createCache("rpkit-stores-bukkit.rpkit_permanent_purchase.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPermanentPurchase::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_permanent_purchase.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create.createTableIfNotExists(RPKIT_PERMANENT_PURCHASE)
                .column(RPKIT_PERMANENT_PURCHASE.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID, SQLDataType.INTEGER)
                .constraints(
                        DSL.constraint("pk_rpkit_permanent_purchase").primaryKey(RPKIT_PERMANENT_PURCHASE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.6.0")
        }
    }

    override fun insert(entity: RPKPermanentPurchase): Int {
        val id = database.getTable(RPKPurchaseTable::class).insert(entity)
        database.create
                .insertInto(
                        RPKIT_PERMANENT_PURCHASE,
                        RPKIT_PERMANENT_PURCHASE.PURCHASE_ID
                )
                .values(
                        id
                )
                .execute()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKPermanentPurchase) {
        database.getTable(RPKPurchaseTable::class).update(entity)
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKPermanentPurchase? {
        val result = database.create
                .select(
                        RPKIT_PURCHASE.STORE_ITEM_ID,
                        RPKIT_PURCHASE.PROFILE_ID,
                        RPKIT_PURCHASE.PURCHASE_DATE,
                        RPKIT_PERMANENT_PURCHASE.PURCHASE_ID
                )
                .from(
                        RPKIT_PURCHASE,
                        RPKIT_PERMANENT_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(id))
                .and(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID.eq(RPKIT_PURCHASE.ID))
                .fetchOne() ?: return null
        val storeItemProvider = plugin.core.serviceManager.getServiceProvider(RPKStoreItemProvider::class)
        val storeItem = storeItemProvider.getStoreItem(result[RPKIT_PURCHASE.STORE_ITEM_ID]) as? RPKPermanentStoreItem
        if (storeItem == null) {
            database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id))
                    .execute()
            database.create
                    .deleteFrom(RPKIT_PERMANENT_PURCHASE)
                    .where(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getProfile(result[RPKIT_PURCHASE.PROFILE_ID])
        if (profile == null) {
            database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id))
                    .execute()
            database.create
                    .deleteFrom(RPKIT_PERMANENT_PURCHASE)
                    .where(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val permanentPurchase = RPKPermanentPurchaseImpl(
                id,
                storeItem,
                profile,
                result[RPKIT_PURCHASE.PURCHASE_DATE].toLocalDateTime()
        )
        cache?.put(id, permanentPurchase)
        return permanentPurchase
    }

    fun get(profile: RPKProfile): List<RPKPermanentPurchase> {
        val result = database.create
                .select(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID)
                .from(
                        RPKIT_PURCHASE,
                        RPKIT_PERMANENT_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(RPKIT_PERMANENT_PURCHASE.ID))
                .and(RPKIT_PURCHASE.PROFILE_ID.eq(profile.id))
                .fetch()
        return result.mapNotNull { row -> get(row[RPKIT_PERMANENT_PURCHASE.PURCHASE_ID]) }
    }

    override fun delete(entity: RPKPermanentPurchase) {
        database.getTable(RPKPurchaseTable::class).delete(entity)
        database.create
                .deleteFrom(RPKIT_PERMANENT_PURCHASE)
                .where(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}