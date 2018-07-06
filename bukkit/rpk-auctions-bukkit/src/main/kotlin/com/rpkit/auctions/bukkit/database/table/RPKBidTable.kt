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

package com.rpkit.auctions.bukkit.database.table

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.auctions.bukkit.auction.RPKAuctionProvider
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.auctions.bukkit.database.jooq.rpkit.Tables.RPKIT_BID
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the bid table.
 */
class RPKBidTable(database: Database, private val plugin: RPKAuctionsBukkit): Table<RPKBid>(database, RPKBid::class) {

    val cache = database.cacheManager.createCache("rpk-auctions-bukkit.rpkit_bid.id", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBid::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong() * plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_BID)
                .column(RPKIT_BID.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_BID.AUCTION_ID, SQLDataType.INTEGER)
                .column(RPKIT_BID.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_BID.AMOUNT, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_bid").primaryKey(RPKIT_BID.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKBid): Int {
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
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKBid) {
        database.create
                .update(RPKIT_BID)
                .set(RPKIT_BID.AUCTION_ID, entity.auction.id)
                .set(RPKIT_BID.CHARACTER_ID, entity.character.id)
                .set(RPKIT_BID.AMOUNT, entity.amount)
                .where(RPKIT_BID.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKBid? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_BID.AUCTION_ID,
                            RPKIT_BID.CHARACTER_ID,
                            RPKIT_BID.AMOUNT
                    )
                    .from(RPKIT_BID)
                    .where(RPKIT_BID.ID.eq(id))
                    .fetchOne() ?: return null
            val auctionProvider = plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class)
            val auctionId = result.get(RPKIT_BID.AUCTION_ID)
            val auction = auctionProvider.getAuction(auctionId)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_BID.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (auction != null && character != null) {
                val bid = RPKBidImpl(
                        id,
                        auction,
                        character,
                        result.get(RPKIT_BID.AMOUNT)
                )
                cache.put(bid.id, bid)
                return bid
            } else {
                database.create
                        .deleteFrom(RPKIT_BID)
                        .where(RPKIT_BID.ID.eq(id))
                        .execute()
                return null
            }
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
        val bids = results.map { result ->
            get(result.get(RPKIT_BID.ID))
        }.filterNotNull()
        return bids
    }

    override fun delete(entity: RPKBid) {
        database.create
                .deleteFrom(RPKIT_BID)
                .where(RPKIT_BID.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }
}