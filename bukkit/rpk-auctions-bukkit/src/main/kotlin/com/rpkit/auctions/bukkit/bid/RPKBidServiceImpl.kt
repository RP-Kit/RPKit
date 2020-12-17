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

package com.rpkit.auctions.bukkit.bid

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.auctions.bukkit.database.table.RPKBidTable
import com.rpkit.auctions.bukkit.event.bid.RPKBukkitBidCreateEvent
import com.rpkit.auctions.bukkit.event.bid.RPKBukkitBidDeleteEvent
import com.rpkit.auctions.bukkit.event.bid.RPKBukkitBidUpdateEvent

/**
 * Bid service implementation.
 */
class RPKBidServiceImpl(override val plugin: RPKAuctionsBukkit) : RPKBidService {

    override fun addBid(bid: RPKBid): Boolean {
        val event = RPKBukkitBidCreateEvent(bid)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return false
        plugin.database.getTable(RPKBidTable::class.java).insert(event.bid)
        return true
    }

    override fun updateBid(bid: RPKBid): Boolean {
        val event = RPKBukkitBidUpdateEvent(bid)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return false
        plugin.database.getTable(RPKBidTable::class.java).update(event.bid)
        return true
    }

    override fun removeBid(bid: RPKBid): Boolean {
        val event = RPKBukkitBidDeleteEvent(bid)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return false
        plugin.database.getTable(RPKBidTable::class.java).delete(event.bid)
        return true
    }

    override fun getBids(auction: RPKAuction): List<RPKBid> {
        return plugin.database.getTable(RPKBidTable::class.java).get(auction)
    }

}