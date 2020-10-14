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
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.purchase.RPKPurchase
import com.rpkit.stores.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKPurchaseTable(
        private val database: Database,
        plugin: RPKStoresBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_purchase.id.enabled")) {
        database.cacheManager.createCache("rpkit-stores-bukkit.rpkit_purchase.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPurchase::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_consumable_purchase.id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKPurchase): Int {
        database.create
                .insertInto(
                        RPKIT_PURCHASE,
                        RPKIT_PURCHASE.STORE_ITEM_ID,
                        RPKIT_PURCHASE.PROFILE_ID,
                        RPKIT_PURCHASE.PURCHASE_DATE
                )
                .values(
                        entity.storeItem.id,
                        entity.profile.id,
                        entity.purchaseDate
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    fun update(entity: RPKPurchase) {
        database.create
                .update(RPKIT_PURCHASE)
                .set(RPKIT_PURCHASE.STORE_ITEM_ID, entity.storeItem.id)
                .set(RPKIT_PURCHASE.PROFILE_ID, entity.profile.id)
                .set(RPKIT_PURCHASE.PURCHASE_DATE, entity.purchaseDate)
                .where(RPKIT_PURCHASE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKPurchase? {
        if (cache?.containsKey(id) == true) return cache[id]
        var purchase: RPKPurchase? = database.getTable(RPKConsumablePurchaseTable::class)[id]
        if (purchase != null) {
            cache?.put(id, purchase)
            return purchase
        } else {
            cache?.remove(id)
        }
        purchase = database.getTable(RPKPermanentPurchaseTable::class)[id]
        if (purchase != null) {
            cache?.put(id, purchase)
            return purchase
        } else {
            cache?.remove(id)
        }
        purchase = database.getTable(RPKTimedPurchaseTable::class)[id]
        if (purchase != null) {
            cache?.put(id, purchase)
            return purchase
        } else {
            cache?.remove(id)
        }
        return null
    }

    fun get(profile: RPKProfile): List<RPKPurchase> {
        return listOf(
                *database.getTable(RPKConsumablePurchaseTable::class).get(profile).toTypedArray(),
                *database.getTable(RPKPermanentPurchaseTable::class).get(profile).toTypedArray(),
                *database.getTable(RPKTimedPurchaseTable::class).get(profile).toTypedArray()
        )
    }

    fun delete(entity: RPKPurchase) {
        database.create
                .deleteFrom(RPKIT_PURCHASE)
                .where(RPKIT_PURCHASE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}