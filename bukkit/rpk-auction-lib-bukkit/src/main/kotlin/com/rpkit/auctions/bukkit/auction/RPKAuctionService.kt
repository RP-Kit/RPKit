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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import com.rpkit.economy.bukkit.currency.RPKCurrency
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

/**
 * Provides auction-related operations.
 */
interface RPKAuctionService : Service {

    /**
     * Adds an auction to be tracked by this auction service.
     *
     * @param auction The auction to add
     * @return Whether adding the auction was successful
     */
    fun addAuction(auction: RPKAuction): Boolean

    /**
     * Creates an auction with the given parameters
     *
     * @param item The item being auctioned
     * @param currency The currency the auction will be held in
     * @param location The location of the auction
     * @param character The character holding the auction
     * @param duration The duration the auction will continue for
     * @param endTime The time at which the auction will end
     * @param startPrice The price at which the auction started
     * @param buyOutPrice The price at which the item will be automatically sold
     * @param noSellPrice The price at which the item will not be sold if the auction does not surpass it
     * @param minimumBidIncrement The minimum increment between bids
     * @param isBiddingOpen Whether bidding on the auction is open
     * @return The auction if creation was successful, otherwise null
     */
    fun createAuction(
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
        isBiddingOpen: Boolean = false
    ): RPKAuction?

    /**
     * Updates an auction's state in data storage.
     *
     * @param auction The auction to update
     * @return Whether updating the auction was successful
     */
    fun updateAuction(auction: RPKAuction): Boolean

    /**
     * Removes an auction from being tracked by this auction service.
     *
     * @param auction The auction to update
     * @return Whether removing the auction was successful
     */
    fun removeAuction(auction: RPKAuction): Boolean

    /**
     * Gets an auction by ID.
     * If there is no auction with the given ID, null is returned.
     *
     * @param id The ID of the auction
     * @return The auction, or null if no auction is found with the given ID
     */
    fun getAuction(id: RPKAuctionId): RPKAuction?

    /**
     * Gets a list of all auctions tracked by this auction service.
     *
     * @return A list of all auctions tracked by this auction service
     */
    fun getAuctions(): List<RPKAuction>

}