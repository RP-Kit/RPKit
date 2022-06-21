/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.store.bukkit.database.create
import com.rpkit.store.bukkit.database.jooq.Tables.RPKIT_PURCHASE
import com.rpkit.store.bukkit.purchase.RPKPurchase
import com.rpkit.store.bukkit.purchase.RPKPurchaseId
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKPurchaseTable(
        private val database: Database,
        private val plugin: RPKStoresBukkit
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

    fun insert(entity: RPKPurchase): CompletableFuture<RPKPurchaseId?> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val storeItemId = entity.storeItem.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.supplyAsync {
            database.create
                .insertInto(
                    RPKIT_PURCHASE,
                    RPKIT_PURCHASE.STORE_ITEM_ID,
                    RPKIT_PURCHASE.PROFILE_ID,
                    RPKIT_PURCHASE.PURCHASE_DATE
                )
                .values(
                    storeItemId.value,
                    profileId.value,
                    entity.purchaseDate
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKPurchaseId(id)
            cache?.set(id, entity)
            return@supplyAsync RPKPurchaseId(id)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert purchase", exception)
            throw exception
        }
    }

    fun update(entity: RPKPurchase): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val storeItemId = entity.storeItem.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_PURCHASE)
                .set(RPKIT_PURCHASE.STORE_ITEM_ID, storeItemId.value)
                .set(RPKIT_PURCHASE.PROFILE_ID, profileId.value)
                .set(RPKIT_PURCHASE.PURCHASE_DATE, entity.purchaseDate)
                .where(RPKIT_PURCHASE.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update purchase", exception)
            throw exception
        }
    }

    operator fun get(id: RPKPurchaseId): CompletableFuture<RPKPurchase?> {
        if (cache?.containsKey(id.value) == true) return CompletableFuture.completedFuture(cache[id.value])
        return CompletableFuture.supplyAsync {
            var purchase: RPKPurchase? = database.getTable(RPKConsumablePurchaseTable::class.java)[id].join()
            if (purchase != null) {
                cache?.set(id.value, purchase)
                return@supplyAsync purchase
            } else {
                cache?.remove(id.value)
            }
            purchase = database.getTable(RPKPermanentPurchaseTable::class.java)[id].join()
            if (purchase != null) {
                cache?.set(id.value, purchase)
                return@supplyAsync purchase
            } else {
                cache?.remove(id.value)
            }
            purchase = database.getTable(RPKTimedPurchaseTable::class.java)[id].join()
            if (purchase != null) {
                cache?.set(id.value, purchase)
                return@supplyAsync purchase
            } else {
                cache?.remove(id.value)
            }
            return@supplyAsync null
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get purchase", exception)
            throw exception
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKPurchase>> {
        return CompletableFuture.supplyAsync {
            listOf(
                *database.getTable(RPKConsumablePurchaseTable::class.java).get(profile).join().toTypedArray(),
                *database.getTable(RPKPermanentPurchaseTable::class.java).get(profile).join().toTypedArray(),
                *database.getTable(RPKTimedPurchaseTable::class.java).get(profile).join().toTypedArray()
            )
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get purchases", exception)
            throw exception
        }
    }

    fun delete(entity: RPKPurchase): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_PURCHASE)
                .where(RPKIT_PURCHASE.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete purchase", exception)
            throw exception
        }
    }
}