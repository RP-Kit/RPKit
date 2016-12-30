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

package com.rpkit.auctions.bukkit.bid

import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.core.service.ServiceProvider

/**
 * Provides bid-related operations.
 */
interface RPKBidProvider: ServiceProvider {

    /**
     * Adds a bid.
     *
     * @param bid The bid to add
     */
    fun addBid(bid: RPKBid)

    /**
     * Updates a bid in data storage.
     *
     * @param bid The bid to update
     */
    fun updateBid(bid: RPKBid)

    /**
     * Removes a bid.
     *
     * @param bid The bid to remove
     */
    fun removeBid(bid: RPKBid)

    /**
     * Gets a list of all bids made for a particular auction.
     *
     * @param auction The auction to list bids for
     */
    fun getBids(auction: RPKAuction): List<RPKBid>

}