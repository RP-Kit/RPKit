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

package com.rpkit.auctions.bukkit.database.table

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.auctions.bukkit.database.create
import com.rpkit.auctions.bukkit.database.jooq.Tables.RPKIT_BID
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services

/**
 * Represents the bid table.
 */
class RPKBidTable(
        private val database: Database,
        private val plugin: RPKAuctionsBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_bid.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-auctions-bukkit.rpkit_bid.id",
            Int::class.javaObjectType,
            RPKBid::class.java,
            plugin.config.getLong("caching.rpkit_bid.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKBid) {
        database.create
                .insertInto(
                        RPKIT_BID,
                        RPKIT_BID.AUCTION_ID,
                        RPKIT_BID.CHARACTER_ID,
                        RPKIT_BID.AMOUNT
                )
                .values(
                        entity.auction.id,
                        entity.character.id,
                        entity.amount
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.set(id, entity)
    }

    fun update(entity: RPKBid) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_BID)
                .set(RPKIT_BID.AUCTION_ID, entity.auction.id)
                .set(RPKIT_BID.CHARACTER_ID, entity.character.id)
                .set(RPKIT_BID.AMOUNT, entity.amount)
                .where(RPKIT_BID.ID.eq(id))
                .execute()
        cache?.set(id, entity)
    }

    operator fun get(id: Int): RPKBid? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
            .select(
                RPKIT_BID.AUCTION_ID,
                RPKIT_BID.CHARACTER_ID,
                RPKIT_BID.AMOUNT
            )
            .from(RPKIT_BID)
            .where(RPKIT_BID.ID.eq(id))
            .fetchOne() ?: return null
        val auctionService = Services[RPKAuctionService::class.java] ?: return null
        val auctionId = result.get(RPKIT_BID.AUCTION_ID)
        val auction = auctionService.getAuction(auctionId)
        val characterService = Services[RPKCharacterService::class.java] ?: return null
        val characterId = result.get(RPKIT_BID.CHARACTER_ID)
        val character = characterService.getCharacter(characterId)
        if (auction != null && character != null) {
            val bid = RPKBidImpl(
                id,
                auction,
                character,
                result.get(RPKIT_BID.AMOUNT)
            )
            cache?.set(id, bid)
            return bid
        } else {
            database.create
                .deleteFrom(RPKIT_BID)
                .where(RPKIT_BID.ID.eq(id))
                .execute()
            return null
        }
    }

    /**
     * Gets all bids for a particular auction.
     *
     * @return A list of the bids made on the auction
     */
    fun get(auction: RPKAuction): List<RPKBid> {
        val results = database.create
                .select(RPKIT_BID.ID)
                .from(RPKIT_BID)
                .where(RPKIT_BID.AUCTION_ID.eq(auction.id))
                .fetch()
        return results.map { result ->
            get(result.get(RPKIT_BID.ID))
        }.filterNotNull()
    }

    fun delete(entity: RPKBid) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_BID)
                .where(RPKIT_BID.ID.eq(entity.id))
                .execute()
        cache?.remove(id)
    }
}