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

import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import java.util.concurrent.CompletableFuture

/**
 * Provides bid-related operations.
 */
interface RPKBidService : Service {

    /**
     * Adds a bid.
     *
     * @param bid The bid to add
     * @return Whether adding the bid was successful
     */
    fun addBid(bid: RPKBid): CompletableFuture<Boolean>

    fun createBid(
        auction: RPKAuction,
        character: RPKCharacter,
        amount: Int
    ): CompletableFuture<RPKBid>

    /**
     * Updates a bid in data storage.
     *
     * @param bid The bid to update
     * @return Whether updating the bid was successful
     */
    fun updateBid(bid: RPKBid): CompletableFuture<Boolean>

    /**
     * Removes a bid.
     *
     * @param bid The bid to remove
     * @return Whether removing the bid was successful
     */
    fun removeBid(bid: RPKBid): CompletableFuture<Boolean>

    /**
     * Gets a list of all bids made for a particular auction.
     *
     * @param auction The auction to list bids for
     */
    fun getBids(auction: RPKAuction): CompletableFuture<List<RPKBid>>

}