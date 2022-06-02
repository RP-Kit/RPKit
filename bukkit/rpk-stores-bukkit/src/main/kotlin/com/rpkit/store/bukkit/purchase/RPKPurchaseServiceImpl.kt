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

package com.rpkit.store.bukkit.purchase

import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.database.table.RPKConsumablePurchaseTable
import com.rpkit.store.bukkit.database.table.RPKPermanentPurchaseTable
import com.rpkit.store.bukkit.database.table.RPKPurchaseTable
import com.rpkit.store.bukkit.database.table.RPKTimedPurchaseTable
import com.rpkit.store.bukkit.event.purchase.RPKBukkitPurchaseCreateEvent
import com.rpkit.store.bukkit.event.purchase.RPKBukkitPurchaseDeleteEvent
import com.rpkit.store.bukkit.event.purchase.RPKBukkitPurchaseUpdateEvent
import com.rpkit.store.bukkit.storeitem.RPKConsumableStoreItem
import com.rpkit.store.bukkit.storeitem.RPKPermanentStoreItem
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItem
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKPurchaseServiceImpl(override val plugin: RPKStoresBukkit) : RPKPurchaseService {

    override fun getPurchases(profile: RPKProfile): CompletableFuture<List<RPKPurchase>> {
        return plugin.database.getTable(RPKPurchaseTable::class.java).get(profile)
    }

    override fun getPurchase(id: RPKPurchaseId): CompletableFuture<RPKPurchase?> {
        return plugin.database.getTable(RPKPurchaseTable::class.java)[id]
    }

    override fun addPurchase(purchase: RPKPurchase): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitPurchaseCreateEvent(purchase, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            when (val eventPurchase = event.purchase) {
                is RPKConsumablePurchase -> plugin.database.getTable(RPKConsumablePurchaseTable::class.java)
                    .insert(eventPurchase).join()
                is RPKPermanentPurchase -> plugin.database.getTable(RPKPermanentPurchaseTable::class.java)
                    .insert(eventPurchase).join()
                is RPKTimedPurchase -> plugin.database.getTable(RPKTimedPurchaseTable::class.java)
                    .insert(eventPurchase).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to add purchase", exception)
            throw exception
        }
    }

    override fun createConsumablePurchase(
        storeItem: RPKConsumableStoreItem,
        profile: RPKProfile,
        purchaseDate: LocalDateTime,
        remainingUses: Int
    ): CompletableFuture<RPKConsumablePurchase> {
        val purchase = RPKConsumablePurchaseImpl(
            null,
            storeItem,
            remainingUses,
            profile,
            purchaseDate
        )
        return addPurchase(purchase).thenApply { purchase }
    }

    override fun createPermanentPurchase(
        storeItem: RPKPermanentStoreItem,
        profile: RPKProfile,
        purchaseDate: LocalDateTime
    ): CompletableFuture<RPKPermanentPurchase> {
        val purchase = RPKPermanentPurchaseImpl(
            null,
            storeItem,
            profile,
            purchaseDate
        )
        return addPurchase(purchase).thenApply { purchase }
    }

    override fun createTimedPurchase(
        storeItem: RPKTimedStoreItem,
        profile: RPKProfile,
        purchaseDate: LocalDateTime
    ): CompletableFuture<RPKTimedPurchase> {
        val purchase = RPKTimedPurchaseImpl(
            null,
            storeItem,
            profile,
            purchaseDate
        )
        return addPurchase(purchase).thenApply { purchase }
    }

    override fun updatePurchase(purchase: RPKPurchase): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitPurchaseUpdateEvent(purchase, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            when (val eventPurchase = event.purchase) {
                is RPKConsumablePurchase -> plugin.database.getTable(RPKConsumablePurchaseTable::class.java)
                    .update(eventPurchase).join()
                is RPKPermanentPurchase -> plugin.database.getTable(RPKPermanentPurchaseTable::class.java)
                    .update(eventPurchase).join()
                is RPKTimedPurchase -> plugin.database.getTable(RPKTimedPurchaseTable::class.java)
                    .update(eventPurchase).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update purchase", exception)
            throw exception
        }
    }

    override fun removePurchase(purchase: RPKPurchase): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitPurchaseDeleteEvent(purchase, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            when (val eventPurchase = event.purchase) {
                is RPKConsumablePurchase -> plugin.database.getTable(RPKConsumablePurchaseTable::class.java)
                    .delete(eventPurchase).join()
                is RPKPermanentPurchase -> plugin.database.getTable(RPKPermanentPurchaseTable::class.java)
                    .delete(eventPurchase).join()
                is RPKTimedPurchase -> plugin.database.getTable(RPKTimedPurchaseTable::class.java)
                    .delete(eventPurchase).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to remove purchase", exception)
            throw exception
        }
    }

}