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
import com.rpkit.store.bukkit.purchase.RPKPurchaseId
import com.rpkit.store.bukkit.storeitem.RPKConsumableStoreItem
import com.rpkit.store.bukkit.storeitem.RPKStoreItemId
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService
import java.util.concurrent.CompletableFuture


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

    fun insert(entity: RPKConsumablePurchase): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val id = database.getTable(RPKPurchaseTable::class.java).insert(entity).join() ?: return@runAsync
            database.create
                .insertInto(
                    RPKIT_CONSUMABLE_PURCHASE,
                    RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID,
                    RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES
                )
                .values(
                    id.value,
                    entity.remainingUses
                )
                .execute()
            entity.id = id
            cache?.set(id.value, entity)
        }
    }

    fun update(entity: RPKConsumablePurchase): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKPurchaseTable::class.java).update(entity).join()
            database.create
                .update(RPKIT_CONSUMABLE_PURCHASE)
                .set(RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES, entity.remainingUses)
                .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }
    }

    operator fun get(id: RPKPurchaseId): CompletableFuture<RPKConsumablePurchase?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        }
        return CompletableFuture.supplyAsync {
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
                .where(RPKIT_PURCHASE.ID.eq(id.value))
                .and(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(RPKIT_PURCHASE.ID))
                .fetchOne() ?: return@supplyAsync null
            val storeItemService = Services[RPKStoreItemService::class.java] ?: return@supplyAsync null
            val storeItem =
                storeItemService.getStoreItem(RPKStoreItemId(result[RPKIT_PURCHASE.STORE_ITEM_ID])).join() as? RPKConsumableStoreItem
            if (storeItem == null) {
                database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id.value))
                    .execute()
                database.create
                    .deleteFrom(RPKIT_CONSUMABLE_PURCHASE)
                    .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id.value))
                    .execute()
                cache?.remove(id.value)
                return@supplyAsync null
            }
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync null
            val profile = profileService.getProfile(RPKProfileId(result[RPKIT_PURCHASE.PROFILE_ID])).join()
            if (profile == null) {
                database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id.value))
                    .execute()
                database.create
                    .deleteFrom(RPKIT_CONSUMABLE_PURCHASE)
                    .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id.value))
                    .execute()
                cache?.remove(id.value)
                return@supplyAsync null
            }
            val consumablePurchase = RPKConsumablePurchaseImpl(
                id,
                storeItem,
                result[RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES],
                profile,
                result[RPKIT_PURCHASE.PURCHASE_DATE]
            )
            cache?.set(id.value, consumablePurchase)
            return@supplyAsync consumablePurchase
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKConsumablePurchase>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID)
                .from(
                    RPKIT_PURCHASE,
                    RPKIT_CONSUMABLE_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(RPKIT_CONSUMABLE_PURCHASE.ID))
                .and(RPKIT_PURCHASE.PROFILE_ID.eq(profileId.value))
                .fetch()
            val purchaseFutures = result.map { row -> get(RPKPurchaseId(row[RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID])) }
            CompletableFuture.allOf(*purchaseFutures.toTypedArray()).join()
            return@supplyAsync purchaseFutures.mapNotNull(CompletableFuture<RPKConsumablePurchase?>::join)
        }
    }

    fun delete(entity: RPKConsumablePurchase): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKPurchaseTable::class.java).delete(entity).join()
            database.create
                .deleteFrom(RPKIT_CONSUMABLE_PURCHASE)
                .where(RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }
    }
}