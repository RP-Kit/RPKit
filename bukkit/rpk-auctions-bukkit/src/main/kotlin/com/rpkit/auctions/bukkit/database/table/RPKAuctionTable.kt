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
import com.rpkit.auctions.bukkit.auction.RPKAuctionImpl
import com.rpkit.auctions.bukkit.database.jooq.Tables.RPKIT_AUCTION
import com.rpkit.auctions.bukkit.database.jooq.Tables.RPKIT_BID
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.bukkit.util.toItemStack
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the auction table.
 */
class RPKAuctionTable(
        private val database: Database,
        private val plugin: RPKAuctionsBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_auction.id.enabled")) {
        database.cacheManager.createCache("rpk-auctions-bukkit.rpkit_auction.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKAuction::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_auction.id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKAuction) {
        database.create
                .insertInto(
                        RPKIT_AUCTION,
                        RPKIT_AUCTION.ITEM,
                        RPKIT_AUCTION.CURRENCY_ID,
                        RPKIT_AUCTION.WORLD,
                        RPKIT_AUCTION.X,
                        RPKIT_AUCTION.Y,
                        RPKIT_AUCTION.Z,
                        RPKIT_AUCTION.YAW,
                        RPKIT_AUCTION.PITCH,
                        RPKIT_AUCTION.CHARACTER_ID,
                        RPKIT_AUCTION.DURATION,
                        RPKIT_AUCTION.END_TIME,
                        RPKIT_AUCTION.START_PRICE,
                        RPKIT_AUCTION.BUY_OUT_PRICE,
                        RPKIT_AUCTION.NO_SELL_PRICE,
                        RPKIT_AUCTION.MINIMUM_BID_INCREMENT,
                        RPKIT_AUCTION.BIDDING_OPEN
                )
                .values(
                        entity.item.toByteArray(),
                        entity.currency.id,
                        entity.location?.world?.name,
                        entity.location?.x,
                        entity.location?.y,
                        entity.location?.z,
                        entity.location?.yaw?.toDouble(),
                        entity.location?.pitch?.toDouble(),
                        entity.character.id,
                        entity.duration,
                        entity.endTime,
                        entity.startPrice,
                        entity.buyOutPrice,
                        entity.noSellPrice,
                        entity.minimumBidIncrement,
                        entity.isBiddingOpen
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKAuction) {
        database.create
                .update(RPKIT_AUCTION)
                .set(RPKIT_AUCTION.ITEM, entity.item.toByteArray())
                .set(RPKIT_AUCTION.CURRENCY_ID, entity.currency.id)
                .set(RPKIT_AUCTION.WORLD, entity.location?.world?.name)
                .set(RPKIT_AUCTION.X, entity.location?.x)
                .set(RPKIT_AUCTION.Y, entity.location?.y)
                .set(RPKIT_AUCTION.Z, entity.location?.z)
                .set(RPKIT_AUCTION.YAW, entity.location?.yaw?.toDouble())
                .set(RPKIT_AUCTION.PITCH, entity.location?.pitch?.toDouble())
                .set(RPKIT_AUCTION.CHARACTER_ID, entity.character.id)
                .set(RPKIT_AUCTION.DURATION, entity.duration)
                .set(RPKIT_AUCTION.END_TIME, entity.endTime)
                .set(RPKIT_AUCTION.START_PRICE, entity.startPrice)
                .set(RPKIT_AUCTION.BUY_OUT_PRICE, entity.buyOutPrice)
                .set(RPKIT_AUCTION.NO_SELL_PRICE, entity.noSellPrice)
                .set(RPKIT_AUCTION.MINIMUM_BID_INCREMENT, entity.minimumBidIncrement)
                .set(RPKIT_AUCTION.BIDDING_OPEN, entity.isBiddingOpen)
                .where(RPKIT_AUCTION.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKAuction? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_AUCTION.ITEM,
                            RPKIT_AUCTION.CURRENCY_ID,
                            RPKIT_AUCTION.WORLD,
                            RPKIT_AUCTION.X,
                            RPKIT_AUCTION.Y,
                            RPKIT_AUCTION.Z,
                            RPKIT_AUCTION.YAW,
                            RPKIT_AUCTION.PITCH,
                            RPKIT_AUCTION.CHARACTER_ID,
                            RPKIT_AUCTION.DURATION,
                            RPKIT_AUCTION.END_TIME,
                            RPKIT_AUCTION.START_PRICE,
                            RPKIT_AUCTION.BUY_OUT_PRICE,
                            RPKIT_AUCTION.NO_SELL_PRICE,
                            RPKIT_AUCTION.MINIMUM_BID_INCREMENT,
                            RPKIT_AUCTION.BIDDING_OPEN
                    )
                    .from(RPKIT_AUCTION)
                    .where(RPKIT_AUCTION.ID.eq(id))
                    .fetchOne() ?: return null
            val currencyService = Services[RPKCurrencyService::class] ?: return null
            val currencyId = result.get(RPKIT_AUCTION.CURRENCY_ID)
            val currency = currencyService.getCurrency(currencyId)
            val characterService = Services[RPKCharacterService::class] ?: return null
            val characterId = result.get(RPKIT_AUCTION.CHARACTER_ID)
            val character = characterService.getCharacter(characterId)
            if (currency != null && character != null) {
                val auction = RPKAuctionImpl(
                        plugin,
                        id,
                        result.get(RPKIT_AUCTION.ITEM).toItemStack(),
                        currency,
                        Location(
                                plugin.server.getWorld(result.get(RPKIT_AUCTION.WORLD)),
                                result.get(RPKIT_AUCTION.X),
                                result.get(RPKIT_AUCTION.Y),
                                result.get(RPKIT_AUCTION.Z),
                                result.get(RPKIT_AUCTION.YAW).toFloat(),
                                result.get(RPKIT_AUCTION.PITCH).toFloat()
                        ),
                        character,
                        result.get(RPKIT_AUCTION.DURATION),
                        result.get(RPKIT_AUCTION.END_TIME),
                        result.get(RPKIT_AUCTION.START_PRICE),
                        result.get(RPKIT_AUCTION.BUY_OUT_PRICE),
                        result.get(RPKIT_AUCTION.NO_SELL_PRICE),
                        result.get(RPKIT_AUCTION.MINIMUM_BID_INCREMENT),
                        result.get(RPKIT_AUCTION.BIDDING_OPEN)
                )
                cache?.put(id, auction)
                return auction
            } else {
                val bidTable = database.getTable(RPKBidTable::class)
                database.create
                        .select(RPKIT_BID.ID)
                        .from(RPKIT_BID)
                        .where(RPKIT_BID.AUCTION_ID.eq(id))
                        .fetch()
                        .map { it[RPKIT_BID.ID] }
                        .mapNotNull { bidId -> bidTable[bidId] }
                        .forEach { bid -> bidTable.delete(bid) }
                database.create
                        .deleteFrom(RPKIT_AUCTION)
                        .where(RPKIT_AUCTION.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets a list of all auctions
     *
     * @return A list containing all auctions
     */
    fun getAll(): List<RPKAuction> {
        val results = database.create
                .select(RPKIT_AUCTION.ID)
                .from(RPKIT_AUCTION)
                .fetch()
        return results.map { result ->
            get(result.get(RPKIT_AUCTION.ID))
        }.filterNotNull()
    }

    fun delete(entity: RPKAuction) {
        for (bid in entity.bids) {
            database.getTable(RPKBidTable::class).delete(bid)
        }
        database.create
                .deleteFrom(RPKIT_AUCTION)
                .where(RPKIT_AUCTION.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}