/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.auctions.bukkit.auction.RPKAuctionId
import com.rpkit.auctions.bukkit.auction.RPKAuctionImpl
import com.rpkit.auctions.bukkit.bid.RPKBidId
import com.rpkit.auctions.bukkit.database.create
import com.rpkit.auctions.bukkit.database.jooq.Tables.RPKIT_AUCTION
import com.rpkit.auctions.bukkit.database.jooq.Tables.RPKIT_BID
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.toByteArray
import com.rpkit.core.bukkit.extension.toItemStack
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level.SEVERE

/**
 * Represents the auction table.
 */
class RPKAuctionTable(
        private val database: Database,
        private val plugin: RPKAuctionsBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_auction.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-auctions-bukkit.rpkit_auction.id",
            Int::class.java,
            RPKAuction::class.java,
            plugin.config.getLong("caching.rpkit_auction.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKAuction): CompletableFuture<Void> {
        val currencyName = entity.currency.name
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_AUCTION,
                    RPKIT_AUCTION.ITEM,
                    RPKIT_AUCTION.CURRENCY_NAME,
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
                    currencyName.value,
                    entity.location?.world,
                    entity.location?.x,
                    entity.location?.y,
                    entity.location?.z,
                    entity.location?.yaw?.toDouble(),
                    entity.location?.pitch?.toDouble(),
                    characterId.value,
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
            entity.id = RPKAuctionId(id)
            cache?.set(id, entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to insert auction", exception)
            throw exception
        }
    }

    fun update(entity: RPKAuction): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        val currencyName = entity.currency.name
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .update(RPKIT_AUCTION)
                .set(RPKIT_AUCTION.ITEM, entity.item.toByteArray())
                .set(RPKIT_AUCTION.CURRENCY_NAME, currencyName.value)
                .set(RPKIT_AUCTION.WORLD, entity.location?.world)
                .set(RPKIT_AUCTION.X, entity.location?.x)
                .set(RPKIT_AUCTION.Y, entity.location?.y)
                .set(RPKIT_AUCTION.Z, entity.location?.z)
                .set(RPKIT_AUCTION.YAW, entity.location?.yaw?.toDouble())
                .set(RPKIT_AUCTION.PITCH, entity.location?.pitch?.toDouble())
                .set(RPKIT_AUCTION.CHARACTER_ID, characterId.value)
                .set(RPKIT_AUCTION.DURATION, entity.duration)
                .set(RPKIT_AUCTION.END_TIME, entity.endTime)
                .set(RPKIT_AUCTION.START_PRICE, entity.startPrice)
                .set(RPKIT_AUCTION.BUY_OUT_PRICE, entity.buyOutPrice)
                .set(RPKIT_AUCTION.NO_SELL_PRICE, entity.noSellPrice)
                .set(RPKIT_AUCTION.MINIMUM_BID_INCREMENT, entity.minimumBidIncrement)
                .set(RPKIT_AUCTION.BIDDING_OPEN, entity.isBiddingOpen)
                .where(RPKIT_AUCTION.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to update auction", exception)
            throw exception
        }
    }

    operator fun get(id: RPKAuctionId): CompletableFuture<out RPKAuction?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_AUCTION.ITEM,
                        RPKIT_AUCTION.CURRENCY_NAME,
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
                    .where(RPKIT_AUCTION.ID.eq(id.value))
                    .fetchOne() ?: return@supplyAsync null
                val currencyService = Services[RPKCurrencyService::class.java] ?: return@supplyAsync null
                val currencyName = result.get(RPKIT_AUCTION.CURRENCY_NAME)
                val currency = currencyService.getCurrency(RPKCurrencyName(currencyName))
                val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync null
                val characterId = result.get(RPKIT_AUCTION.CHARACTER_ID)
                val character = characterService.getCharacter(RPKCharacterId(characterId)).join()
                if (currency != null && character != null) {
                    val auction = RPKAuctionImpl(
                        plugin,
                        id,
                        result.get(RPKIT_AUCTION.ITEM).toItemStack(),
                        currency,
                        RPKLocation(
                            result.get(RPKIT_AUCTION.WORLD),
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
                    cache?.set(id.value, auction)
                    return@supplyAsync auction
                } else {
                    val bidTable = database.getTable(RPKBidTable::class.java)
                    database.create
                        .select(RPKIT_BID.ID)
                        .from(RPKIT_BID)
                        .where(RPKIT_BID.AUCTION_ID.eq(id.value))
                        .fetch()
                        .map { it[RPKIT_BID.ID] }
                        .mapNotNull { bidId -> bidTable[RPKBidId(bidId)].join() }
                        .forEach { bid -> bidTable.delete(bid) }
                    database.create
                        .deleteFrom(RPKIT_AUCTION)
                        .where(RPKIT_AUCTION.ID.eq(id.value))
                        .execute()
                    return@supplyAsync null
                }
            }.exceptionally { exception ->
                plugin.logger.log(SEVERE, "Failed to get auction", exception)
                throw exception
            }
        }
    }

    /**
     * Gets a list of all auctions
     *
     * @return A list containing all auctions
     */
    fun getAll(): CompletableFuture<List<RPKAuction>> {
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_AUCTION.ID)
                .from(RPKIT_AUCTION)
                .fetch()
            results.map { result ->
                get(RPKAuctionId(result[RPKIT_AUCTION.ID])).join()
            }.filterNotNull()
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to get all auctions", exception)
            throw exception
        }
    }

    fun delete(entity: RPKAuction): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        val bidTable = database.getTable(RPKBidTable::class.java)
        return runAsync {
            entity.bids.thenAccept { bids ->
                CompletableFuture.allOf(*bids.map { bidTable.delete(it) }.toTypedArray()).join()
            }
            database.create
                .deleteFrom(RPKIT_AUCTION)
                .where(RPKIT_AUCTION.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to delete auction", exception)
            throw exception
        }
    }

    fun delete(characterId: RPKCharacterId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_AUCTION)
            .where(RPKIT_AUCTION.CHARACTER_ID.eq(characterId.value))
            .execute()
        cache?.removeMatching { it.character.id?.value == characterId.value }
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete auctions for character", exception)
        throw exception
    }

}