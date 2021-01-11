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
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_CONSUMABLE_PURCHASE
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.store.bukkit.purchase.RPKConsumablePurchase
import com.rpkit.store.bukkit.purchase.RPKConsumablePurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKConsumableStoreItem
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService


class RPKConsumablePurchaseTable(private val database: Database, private val plugin: RPKStoresBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_consumable_purchase.id.enabled")) {
        database.cacheManager.createCache(
            "rpkit-stores-bukkit.rpkit_consumable_purchase.id",
            Int::class.javaObjectType,
            RPKConsumablePurchase::class.java,
            plugin.config.getLong("caching.rpkit_consumable_purchase.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKConsumablePurchase) {
        val id = database.getTable(RPKPurchaseTable::class.java).insert(entity) ?: return
        database.create
                .insertInto(
                        RPKIT_CONSUMABLE_PURCHASE,
                        RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID,
                        RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES
                )
                .values(
                        id,
                        entity.remainingUses
                )
                .execute()
        entity.id = id
        cache?.set(id, entity)
    }

    fun update(entity: RPKConsumablePurchase) {
        val id = entity.id ?: return
        database.getTable(RPKPurchaseTable::class.java).update(entity)
        database.create
                .update(RPKIT_CONSUMABLE_PURCHASE)
                .set(RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES, entity.remainingUses)
                .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id))
                .execute()
        cache?.set(id, entity)
    }

    operator fun get(id: Int): RPKConsumablePurchase? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_PURCHASE.STORE_ITEM_ID,
                        RPKIT_PURCHASE.PROFILE_ID,
                        RPKIT_PURCHASE.PURCHASE_DATE,
                        RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID,
                        RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES
                )
                .from(
                        RPKIT_PURCHASE,
                        RPKIT_CONSUMABLE_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(id))
                .and(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(RPKIT_PURCHASE.ID))
                .fetchOne() ?: return null
        val storeItemService = Services[RPKStoreItemService::class.java] ?: return null
        val storeItem = storeItemService.getStoreItem(result[RPKIT_PURCHASE.STORE_ITEM_ID]) as? RPKConsumableStoreItem
        if (storeItem == null) {
            database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id))
                    .execute()
            database.create
                    .deleteFrom(RPKIT_CONSUMABLE_PURCHASE)
                    .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val profileService = Services[RPKProfileService::class.java] ?: return null
        val profile = profileService.getProfile(RPKProfileId(result[RPKIT_PURCHASE.PROFILE_ID]))
        if (profile == null) {
            database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id))
                    .execute()
            database.create
                    .deleteFrom(RPKIT_CONSUMABLE_PURCHASE)
                    .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val consumablePurchase = RPKConsumablePurchaseImpl(
                id,
                storeItem,
                result[RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES],
                profile,
                result[RPKIT_PURCHASE.PURCHASE_DATE]
        )
        cache?.set(id, consumablePurchase)
        return consumablePurchase
    }

    fun get(profile: RPKProfile): List<RPKConsumablePurchase> {
        val profileId = profile.id ?: return emptyList()
        val result = database.create
                .select(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID)
                .from(
                        RPKIT_PURCHASE,
                        RPKIT_CONSUMABLE_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(RPKIT_CONSUMABLE_PURCHASE.ID))
                .and(RPKIT_PURCHASE.PROFILE_ID.eq(profileId.value))
                .fetch()
        return result.mapNotNull { row -> get(row[RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID]) }
    }

    fun delete(entity: RPKConsumablePurchase) {
        val id = entity.id ?: return
        database.getTable(RPKPurchaseTable::class.java).delete(entity)
        database.create
                .deleteFrom(RPKIT_CONSUMABLE_PURCHASE)
                .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id))
                .execute()
        cache?.remove(id)
    }
}