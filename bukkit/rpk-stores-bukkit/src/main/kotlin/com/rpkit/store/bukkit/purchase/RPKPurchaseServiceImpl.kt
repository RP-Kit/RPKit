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


class RPKPurchaseServiceImpl(override val plugin: RPKStoresBukkit) : RPKPurchaseService {

    override fun getPurchases(profile: RPKProfile): List<RPKPurchase> {
        return plugin.database.getTable(RPKPurchaseTable::class.java).get(profile)
    }

    override fun getPurchase(id: RPKPurchaseId): RPKPurchase? {
        return plugin.database.getTable(RPKPurchaseTable::class.java)[id]
    }

    override fun addPurchase(purchase: RPKPurchase) {
        val event = RPKBukkitPurchaseCreateEvent(purchase)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventPurchase = event.purchase
        when (eventPurchase) {
            is RPKConsumablePurchase -> plugin.database.getTable(RPKConsumablePurchaseTable::class.java).insert(eventPurchase)
            is RPKPermanentPurchase -> plugin.database.getTable(RPKPermanentPurchaseTable::class.java).insert(eventPurchase)
            is RPKTimedPurchase -> plugin.database.getTable(RPKTimedPurchaseTable::class.java).insert(eventPurchase)
        }
    }

    override fun createConsumablePurchase(
        storeItem: RPKConsumableStoreItem,
        profile: RPKProfile,
        purchaseDate: LocalDateTime,
        remainingUses: Int
    ): RPKConsumablePurchase {
        val purchase = RPKConsumablePurchaseImpl(
            null,
            storeItem,
            remainingUses,
            profile,
            purchaseDate
        )
        addPurchase(purchase)
        return purchase
    }

    override fun createPermanentPurchase(
        storeItem: RPKPermanentStoreItem,
        profile: RPKProfile,
        purchaseDate: LocalDateTime
    ): RPKPermanentPurchase {
        val purchase = RPKPermanentPurchaseImpl(
            null,
            storeItem,
            profile,
            purchaseDate
        )
        addPurchase(purchase)
        return purchase
    }

    override fun createTimedPurchase(
        storeItem: RPKTimedStoreItem,
        profile: RPKProfile,
        purchaseDate: LocalDateTime
    ): RPKTimedPurchase {
        val purchase = RPKTimedPurchaseImpl(
            null,
            storeItem,
            profile,
            purchaseDate
        )
        addPurchase(purchase)
        return purchase
    }

    override fun updatePurchase(purchase: RPKPurchase) {
        val event = RPKBukkitPurchaseUpdateEvent(purchase)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventPurchase = event.purchase
        when (eventPurchase) {
            is RPKConsumablePurchase -> plugin.database.getTable(RPKConsumablePurchaseTable::class.java).update(eventPurchase)
            is RPKPermanentPurchase -> plugin.database.getTable(RPKPermanentPurchaseTable::class.java).update(eventPurchase)
            is RPKTimedPurchase -> plugin.database.getTable(RPKTimedPurchaseTable::class.java).update(eventPurchase)
        }
    }

    override fun removePurchase(purchase: RPKPurchase) {
        val event = RPKBukkitPurchaseDeleteEvent(purchase)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventPurchase = event.purchase
        when (eventPurchase) {
            is RPKConsumablePurchase -> plugin.database.getTable(RPKConsumablePurchaseTable::class.java).delete(eventPurchase)
            is RPKPermanentPurchase -> plugin.database.getTable(RPKPermanentPurchaseTable::class.java).delete(eventPurchase)
            is RPKTimedPurchase -> plugin.database.getTable(RPKTimedPurchaseTable::class.java).delete(eventPurchase)
        }
    }

}