/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.auctions.bukkit.auction

import com.seventh_root.elysium.auctions.bukkit.ElysiumAuctionsBukkit
import com.seventh_root.elysium.auctions.bukkit.database.table.ElysiumAuctionTable

/**
 * Auction provider implementation.
 */
class ElysiumAuctionProviderImpl(private val plugin: ElysiumAuctionsBukkit): ElysiumAuctionProvider {

    override fun getAuction(id: Int): ElysiumAuction? {
        return plugin.core.database.getTable(ElysiumAuctionTable::class)[id]
    }

    override fun addAuction(auction: ElysiumAuction) {
        plugin.core.database.getTable(ElysiumAuctionTable::class).insert(auction)
    }

    override fun updateAuction(auction: ElysiumAuction) {
        plugin.core.database.getTable(ElysiumAuctionTable::class).update(auction)
    }

    override fun removeAuction(auction: ElysiumAuction) {
        plugin.core.database.getTable(ElysiumAuctionTable::class).delete(auction)
    }

    override fun getAuctions(): List<ElysiumAuction> {
        return plugin.core.database.getTable(ElysiumAuctionTable::class).getAll()
    }
}