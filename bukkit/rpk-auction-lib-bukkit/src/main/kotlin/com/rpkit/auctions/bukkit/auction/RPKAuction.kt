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

package com.rpkit.auctions.bukkit.auction

import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity
import com.rpkit.economy.bukkit.currency.RPKCurrency
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

/**
 * Represents an auction.
 */
interface RPKAuction: Entity {

    /**
     * The item being auctioned.
     */
    val item: ItemStack

    /**
     * The currency with which the auction is being performed in.
     */
    val currency: RPKCurrency

    /**
     * The location at which the auction is being performed.
     * How close a player must be to the location is currently implementation-defined.
     * If the auction is global, this may be null.
     */
    val location: Location?

    /**
     * The character running the auction.
     * This character put the item up for auction and will receive the money once the auction has concluded.
     */
    val character: RPKCharacter

    /**
     * A list of bids made on the auction.
     * This may not be assumed to be sorted in any way, as implementations may order their bids differently.
     */
    val bids: List<RPKBid>

    /**
     * The duration of the auction, in milliseconds.
     */
    val duration: Long

    /**
     * The end time of the auction, as a system timestamp in milliseconds, as retrievable by [System.currentTimeMillis]
     */
    val endTime: Long

    /**
     * The start price of the auction. Bids must start at this price plus the [minimumBidIncrement]
     */
    val startPrice: Int

    /**
     * The buy-out price of the auction.
     * If not null, auctions will end when a bid reaches this price or higher.
     */
    val buyOutPrice: Int?

    /**
     * The no-sell price of the auction.
     * If not null, when the auction reaches it's conclusion, if the highest bid is still under this price, the item will not be sold.
     */
    val noSellPrice: Int?

    /**
     * The minimum bid increment of the auction.
     * Every bid must be at least this much higher than the previous one.
     * If there are no bids, the first bid must be at least the [startPrice] plus the [minimumBidIncrement]
     */
    val minimumBidIncrement: Int

    /**
     * Whether bidding is currently open for this auction.
     * Bids may only be added when the bidding is open, i.e. this value is true.
     */
    val isBiddingOpen: Boolean

    /**
     * Adds a bid to the auction.
     *
     * @param bid The bid to add
     */
    fun addBid(bid: RPKBid)

    /**
     * Opens bidding on the auction, performing any required setup.
     * Behaviour when bidding is already open my vary by implementation.
     */
    fun openBidding()

    /**
     * Closes bidding on the auction, performing any cleanup and closing operations.
     * If the highest bid is high enough, i.e. higher than any [noSellPrice] if set, then the item will be given to the character which made the bid.
     */
    fun closeBidding()

}