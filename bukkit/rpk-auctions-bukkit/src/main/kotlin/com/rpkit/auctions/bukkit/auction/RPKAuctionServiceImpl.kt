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

package com.rpkit.auctions.bukkit.auction

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.database.table.RPKAuctionTable
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionCreateEvent
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionDeleteEvent
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionUpdateEvent
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.economy.bukkit.currency.RPKCurrency
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

/**
 * Auction service implementation.
 */
class RPKAuctionServiceImpl(override val plugin: RPKAuctionsBukkit) : RPKAuctionService {

    override fun getAuction(id: RPKAuctionId): CompletableFuture<RPKAuction?> {
        return plugin.database.getTable(RPKAuctionTable::class.java)[id]
    }

    override fun addAuction(auction: RPKAuction): CompletableFuture<Boolean> {
        val event = RPKBukkitAuctionCreateEvent(auction)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(false)
        return plugin.database.getTable(RPKAuctionTable::class.java).insert(event.auction)
            .thenApply { true }
    }

    override fun createAuction(
        item: ItemStack,
        currency: RPKCurrency,
        location: Location?,
        character: RPKCharacter,
        duration: Long,
        endTime: Long,
        startPrice: Int,
        buyOutPrice: Int?,
        noSellPrice: Int?,
        minimumBidIncrement: Int,
        isBiddingOpen: Boolean
    ): CompletableFuture<RPKAuction?> {
        val auction = RPKAuctionImpl(
            plugin,
            null,
            item,
            currency,
            location,
            character,
            duration,
            endTime,
            startPrice,
            buyOutPrice,
            noSellPrice,
            minimumBidIncrement,
            isBiddingOpen
        )
        return addAuction(auction)
            .thenApply { success -> if (success) auction else null }
    }

    override fun updateAuction(auction: RPKAuction): CompletableFuture<Boolean> {
        val event = RPKBukkitAuctionUpdateEvent(auction)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(false)
        return plugin.database.getTable(RPKAuctionTable::class.java).update(event.auction)
            .thenApply { true }
    }

    override fun removeAuction(auction: RPKAuction): CompletableFuture<Boolean> {
        val event = RPKBukkitAuctionDeleteEvent(auction)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(false)
        return plugin.database.getTable(RPKAuctionTable::class.java).delete(event.auction)
            .thenApply { true }
    }

    override fun getAuctions(): CompletableFuture<List<RPKAuction>> {
        return plugin.database.getTable(RPKAuctionTable::class.java).getAll()
    }
}