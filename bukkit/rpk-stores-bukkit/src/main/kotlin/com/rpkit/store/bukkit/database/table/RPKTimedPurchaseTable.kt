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
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.purchase.RPKTimedPurchase
import com.rpkit.store.bukkit.purchase.RPKTimedPurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItem
import com.rpkit.stores.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.stores.bukkit.database.jooq.Tables.RPKIT_TIMED_PURCHASE
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKTimedPurchaseTable(private val database: Database, plugin: RPKStoresBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_timed_purchase.id.enabled")) {
        database.cacheManager.createCache("rpkit-stores-bukkit.rpkit_timed_purchase.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKTimedPurchase::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_timed_purchase.id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKTimedPurchase) {
        val id = database.getTable(RPKPurchaseTable::class).insert(entity)
        database.create
                .insertInto(
                        RPKIT_TIMED_PURCHASE,
                        RPKIT_TIMED_PURCHASE.PURCHASE_ID
                )
                .values(
                        id
                )
                .execute()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKTimedPurchase) {
        database.getTable(RPKPurchaseTable::class).update(entity)
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKTimedPurchase? {
        val result = database.create
                .select(
                        RPKIT_PURCHASE.STORE_ITEM_ID,
                        RPKIT_PURCHASE.PROFILE_ID,
                        RPKIT_PURCHASE.PURCHASE_DATE,
                        RPKIT_TIMED_PURCHASE.PURCHASE_ID
                )
                .from(RPKIT_TIMED_PURCHASE)
                .where(RPKIT_PURCHASE.ID.eq(id))
                .and(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(RPKIT_PURCHASE.ID))
                .fetchOne() ?: return null
        val storeItemService = Services[RPKStoreItemService::class] ?: return null
        val storeItem = storeItemService.getStoreItem(result[RPKIT_PURCHASE.STORE_ITEM_ID]) as? RPKTimedStoreItem
        if (storeItem == null) {
            database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id))
                    .execute()
            database.create
                    .deleteFrom(RPKIT_TIMED_PURCHASE)
                    .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val profileService = Services[RPKProfileService::class] ?: return null
        val profile = profileService.getProfile(result[RPKIT_PURCHASE.PROFILE_ID])
        if (profile == null) {
            database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id))
                    .execute()
            database.create
                    .deleteFrom(RPKIT_TIMED_PURCHASE)
                    .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val timedPurchase = RPKTimedPurchaseImpl(
                id,
                storeItem,
                profile,
                result[RPKIT_PURCHASE.PURCHASE_DATE]
        )
        cache?.put(id, timedPurchase)
        return timedPurchase
    }

    fun get(profile: RPKProfile): List<RPKTimedPurchase> {
        val result = database.create
                .select(RPKIT_TIMED_PURCHASE.PURCHASE_ID)
                .from(
                        RPKIT_PURCHASE,
                        RPKIT_TIMED_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(RPKIT_TIMED_PURCHASE.ID))
                .and(RPKIT_PURCHASE.PROFILE_ID.eq(profile.id))
                .fetch()
        return result.mapNotNull { row -> get(row[RPKIT_TIMED_PURCHASE.PURCHASE_ID]) }
    }

    fun delete(entity: RPKTimedPurchase) {
        database.getTable(RPKPurchaseTable::class).delete(entity)
        database.create
                .deleteFrom(RPKIT_TIMED_PURCHASE)
                .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}