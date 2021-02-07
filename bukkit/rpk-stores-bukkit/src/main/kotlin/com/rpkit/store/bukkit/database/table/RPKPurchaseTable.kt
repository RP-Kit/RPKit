/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.store.bukkit.purchase.RPKPurchase
import com.rpkit.store.bukkit.purchase.RPKPurchaseId


class RPKPurchaseTable(
        private val database: Database,
        plugin: RPKStoresBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_purchase.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_purchase.id",
            Int::class.javaObjectType,
            RPKPurchase::class.java,
            plugin.config.getLong("caching.rpkit_consumable_purchase.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPurchase): RPKPurchaseId? {
        val profileId = entity.profile.id ?: return null
        database.create
                .insertInto(
                        RPKIT_PURCHASE,
                        RPKIT_PURCHASE.STORE_ITEM_ID,
                        RPKIT_PURCHASE.PROFILE_ID,
                        RPKIT_PURCHASE.PURCHASE_DATE
                )
                .values(
                        entity.storeItem.id,
                        profileId.value,
                        entity.purchaseDate
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKPurchaseId(id)
        cache?.set(id, entity)
        return RPKPurchaseId(id)
    }

    fun update(entity: RPKPurchase) {
        val id = entity.id ?: return
        val profileId = entity.profile.id ?: return
        database.create
                .update(RPKIT_PURCHASE)
                .set(RPKIT_PURCHASE.STORE_ITEM_ID, entity.storeItem.id)
                .set(RPKIT_PURCHASE.PROFILE_ID, profileId.value)
                .set(RPKIT_PURCHASE.PURCHASE_DATE, entity.purchaseDate)
                .where(RPKIT_PURCHASE.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
    }

    operator fun get(id: RPKPurchaseId): RPKPurchase? {
        if (cache?.containsKey(id.value) == true) return cache[id.value]
        var purchase: RPKPurchase? = database.getTable(RPKConsumablePurchaseTable::class.java)[id]
        if (purchase != null) {
            cache?.set(id.value, purchase)
            return purchase
        } else {
            cache?.remove(id.value)
        }
        purchase = database.getTable(RPKPermanentPurchaseTable::class.java)[id]
        if (purchase != null) {
            cache?.set(id.value, purchase)
            return purchase
        } else {
            cache?.remove(id.value)
        }
        purchase = database.getTable(RPKTimedPurchaseTable::class.java)[id]
        if (purchase != null) {
            cache?.set(id.value, purchase)
            return purchase
        } else {
            cache?.remove(id.value)
        }
        return null
    }

    fun get(profile: RPKProfile): List<RPKPurchase> {
        return listOf(
                *database.getTable(RPKConsumablePurchaseTable::class.java).get(profile).toTypedArray(),
                *database.getTable(RPKPermanentPurchaseTable::class.java).get(profile).toTypedArray(),
                *database.getTable(RPKTimedPurchaseTable::class.java).get(profile).toTypedArray()
        )
    }

    fun delete(entity: RPKPurchase) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_PURCHASE)
                .where(RPKIT_PURCHASE.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }
}