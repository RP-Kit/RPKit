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
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PERMANENT_PURCHASE
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.store.bukkit.purchase.RPKPermanentPurchase
import com.rpkit.store.bukkit.purchase.RPKPermanentPurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKPermanentStoreItem
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService


class RPKPermanentPurchaseTable(
        private val database: Database,
        plugin: RPKStoresBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_permanent_purchase.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_permanent_purchase.id",
            Int::class.javaObjectType,
            RPKPermanentPurchase::class.java,
            plugin.config.getLong("caching.rpkit_permanent_purchase.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPermanentPurchase) {
        val id = database.getTable(RPKPurchaseTable::class.java).insert(entity)
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
        cache?.set(id, entity)
    }

    fun update(entity: RPKPermanentPurchase) {
        val id = entity.id ?: return
        database.getTable(RPKPurchaseTable::class.java).update(entity)
        cache?.set(id, entity)
    }

    operator fun get(id: Int): RPKPermanentPurchase? {
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
        val storeItemService = Services[RPKStoreItemService::class.java] ?: return null
        val storeItem = storeItemService.getStoreItem(result[RPKIT_PURCHASE.STORE_ITEM_ID]) as? RPKPermanentStoreItem
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
        val profileService = Services[RPKProfileService::class.java] ?: return null
        val profile = profileService.getProfile(result[RPKIT_PURCHASE.PROFILE_ID])
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
                result[RPKIT_PURCHASE.PURCHASE_DATE]
        )
        cache?.set(id, permanentPurchase)
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

    fun delete(entity: RPKPermanentPurchase) {
        val id = entity.id ?: return
        database.getTable(RPKPurchaseTable::class.java).delete(entity)
        database.create
                .deleteFrom(RPKIT_PERMANENT_PURCHASE)
                .where(RPKIT_PERMANENT_PURCHASE.PURCHASE_ID.eq(id))
                .execute()
        cache?.remove(id)
    }
}