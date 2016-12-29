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

import com.seventh_root.elysium.core.service.ServiceProvider

/**
 * Provides auction-related operations.
 */
interface ElysiumAuctionProvider: ServiceProvider {

    /**
     * Adds an auction to be tracked by this auction provider.
     *
     * @param auction The auction to add
     */
    fun addAuction(auction: ElysiumAuction)

    /**
     * Updates an auction's state in data storage.
     *
     * @param auction The auction to update
     */
    fun updateAuction(auction: ElysiumAuction)

    /**
     * Removes an auction from being tracked by this auction provider.
     *
     * @param auction The auction to update
     */
    fun removeAuction(auction: ElysiumAuction)

    /**
     * Gets an auction by ID.
     * If there is no auction with the given ID, null is returned.
     *
     * @param id The ID of the auction
     * @return The auction, or null if no auction is found with the given ID
     */
    fun getAuction(id: Int): ElysiumAuction?

    /**
     * Gets a list of all auctions tracked by this auction provider.
     *
     * @return A list of all auctions tracked by this auction provider
     */
    fun getAuctions(): List<ElysiumAuction>

}