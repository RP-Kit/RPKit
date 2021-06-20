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
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_TIMED_PURCHASE
import com.rpkit.store.bukkit.purchase.RPKPurchaseId
import com.rpkit.store.bukkit.purchase.RPKTimedPurchase
import com.rpkit.store.bukkit.purchase.RPKTimedPurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemId
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItem
import java.util.concurrent.CompletableFuture


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

    fun insert(entity: RPKTimedPurchase): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val id = database.getTable(RPKPurchaseTable::class.java).insert(entity).join() ?: return@runAsync
            database.create
                .insertInto(
                    RPKIT_TIMED_PURCHASE,
                    RPKIT_TIMED_PURCHASE.PURCHASE_ID
                )
                .values(
                    id.value
                )
                .execute()
            entity.id = id
            cache?.set(id.value, entity)
        }
    }

    fun update(entity: RPKTimedPurchase): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKPurchaseTable::class.java).update(entity).join()
            cache?.set(id.value, entity)
        }
    }

    operator fun get(id: RPKPurchaseId): CompletableFuture<RPKTimedPurchase?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_PURCHASE.STORE_ITEM_ID,
                    RPKIT_PURCHASE.PROFILE_ID,
                    RPKIT_PURCHASE.PURCHASE_DATE,
                    RPKIT_TIMED_PURCHASE.PURCHASE_ID
                )
                .from(RPKIT_TIMED_PURCHASE)
                .where(RPKIT_PURCHASE.ID.eq(id.value))
                .and(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(RPKIT_PURCHASE.ID))
                .fetchOne() ?: return@supplyAsync null
            val storeItemService = Services[RPKStoreItemService::class.java] ?: return@supplyAsync null
            val storeItem = storeItemService.getStoreItem(RPKStoreItemId(result[RPKIT_PURCHASE.STORE_ITEM_ID])).join() as? RPKTimedStoreItem
            if (storeItem == null) {
                database.create
                    .deleteFrom(RPKIT_PURCHASE)
                    .where(RPKIT_PURCHASE.ID.eq(id.value))
                    .execute()
                database.create
                    .deleteFrom(RPKIT_TIMED_PURCHASE)
                    .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(id.value))
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
                    .deleteFrom(RPKIT_TIMED_PURCHASE)
                    .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(id.value))
                    .execute()
                cache?.remove(id.value)
                return@supplyAsync null
            }
            val timedPurchase = RPKTimedPurchaseImpl(
                id,
                storeItem,
                profile,
                result[RPKIT_PURCHASE.PURCHASE_DATE]
            )
            cache?.set(id.value, timedPurchase)
            return@supplyAsync timedPurchase
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKTimedPurchase>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_TIMED_PURCHASE.PURCHASE_ID)
                .from(
                    RPKIT_PURCHASE,
                    RPKIT_TIMED_PURCHASE
                )
                .where(RPKIT_PURCHASE.ID.eq(RPKIT_TIMED_PURCHASE.ID))
                .and(RPKIT_PURCHASE.PROFILE_ID.eq(profileId.value))
                .fetch()
            val purchaseFutures = result.map { row -> get(RPKPurchaseId(row[RPKIT_TIMED_PURCHASE.PURCHASE_ID])) }
            CompletableFuture.allOf(*purchaseFutures.toTypedArray()).join()
            return@supplyAsync purchaseFutures.mapNotNull(CompletableFuture<RPKTimedPurchase?>::join)
        }
    }

    fun delete(entity: RPKTimedPurchase): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.getTable(RPKPurchaseTable::class.java).delete(entity).join()
            database.create
                .deleteFrom(RPKIT_TIMED_PURCHASE)
                .where(RPKIT_TIMED_PURCHASE.PURCHASE_ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }
    }
}