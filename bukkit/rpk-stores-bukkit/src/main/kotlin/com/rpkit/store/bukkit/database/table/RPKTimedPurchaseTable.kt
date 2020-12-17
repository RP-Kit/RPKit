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
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_TIMED_PURCHASE
import com.rpkit.store.bukkit.purchase.RPKTimedPurchase
import com.rpkit.store.bukkit.purchase.RPKTimedPurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItem


class RPKTimedPurchaseTable(private val database: Database, plugin: RPKStoresBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_timed_purchase.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_timed_purchase.id",
            Int::class.javaObjectType,
            RPKTimedPurchase::class.java,
            plugin.config.getLong("caching.rpkit_timed_purchase.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKTimedPurchase) {
        val id = database.getTable(RPKPurchaseTable::class.java).insert(entity)
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
        cache?.set(id, entity)
    }

    fun update(entity: RPKTimedPurchase) {
        val id = entity.id ?: return
        database.getTable(RPKPurchaseTable::class.java).update(entity)
        cache?.set(id, entity)
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
        val storeItemService = Services[RPKStoreItemService::class.java] ?: return null
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
        val profileService = Services[RPKProfileService::class.java] ?: return null
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
        cache?.set(id, timedPurchase)
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
        val id = entity.id ?: return
        database.getTable(RPKPurchaseTable::class.java).delete(entity)
        database.create
                .deleteFrom(RPKIT_TIMED_PURCHASE)
                .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(id))
                .execute()
        cache?.remove(id)
    }
}