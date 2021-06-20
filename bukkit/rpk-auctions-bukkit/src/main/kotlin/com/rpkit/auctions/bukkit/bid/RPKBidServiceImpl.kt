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

package com.rpkit.auctions.bukkit.bid

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.auctions.bukkit.database.table.RPKBidTable
import com.rpkit.auctions.bukkit.event.bid.RPKBukkitBidCreateEvent
import com.rpkit.auctions.bukkit.event.bid.RPKBukkitBidDeleteEvent
import com.rpkit.auctions.bukkit.event.bid.RPKBukkitBidUpdateEvent
import com.rpkit.characters.bukkit.character.RPKCharacter
import java.util.concurrent.CompletableFuture

/**
 * Bid service implementation.
 */
class RPKBidServiceImpl(override val plugin: RPKAuctionsBukkit) : RPKBidService {

    override fun addBid(bid: RPKBid): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            val event = RPKBukkitBidCreateEvent(bid, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@supplyAsync false
            return@supplyAsync plugin.database.getTable(RPKBidTable::class.java).insert(event.bid)
                .thenApply { true }.join()
        }
    }

    override fun createBid(auction: RPKAuction, character: RPKCharacter, amount: Int): CompletableFuture<RPKBid> {
        val bid = RPKBidImpl(
            null,
            auction,
            character,
            amount
        )
        return addBid(bid)
            .thenApply { success -> if (success) bid else null }
    }

    override fun updateBid(bid: RPKBid): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            val event = RPKBukkitBidUpdateEvent(bid, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@supplyAsync false
            return@supplyAsync plugin.database.getTable(RPKBidTable::class.java).update(event.bid)
                .thenApply { true }.join()
        }
    }

    override fun removeBid(bid: RPKBid): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            val event = RPKBukkitBidDeleteEvent(bid, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@supplyAsync false
            return@supplyAsync plugin.database.getTable(RPKBidTable::class.java).delete(event.bid)
                .thenApply { true }.join()
        }
    }

    override fun getBids(auction: RPKAuction): CompletableFuture<List<RPKBid>> {
        return plugin.database.getTable(RPKBidTable::class.java).get(auction)
    }

}