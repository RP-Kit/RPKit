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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents the bid table.
 */
class RPKBidTable: Table<RPKBid> {

    private val plugin: RPKAuctionsBukkit
    val cacheManager: CacheManager
    val cache: Cache<Int, RPKBid>
    val auctionCache: Cache<Int, MutableList<*>>

    constructor(database: Database, plugin: RPKAuctionsBukkit): super(database, RPKBid::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBid::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong() * plugin.server.maxPlayers.toLong())))
        auctionCache = cacheManager.createCache("auctionCache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_bid(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "auction_id INTEGER," +
                            "character_id INTEGER," +
                            "amount INTEGER" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKBid): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_bid(auction_id, character_id, amount) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.auction.id)
                statement.setInt(2, entity.character.id)
                statement.setInt(3, entity.amount)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKBid) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_bid SET auction_id = ?, character_id = ?, amount = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.auction.id)
                statement.setInt(2, entity.character.id)
                statement.setInt(3, entity.amount)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKBid? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var bid: RPKBid? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, auction_id, character_id, amount FROM rpkit_bid WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalBid = RPKBidImpl(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class).getAuction(resultSet.getInt("auction_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                                resultSet.getInt("amount")
                        )
                        bid = finalBid
                        cache.put(finalBid.id, finalBid)
                    }
                }
            }
            return bid
        }
    }

    /**
     * Gets all bids for a particular auction.
     *
     * @return A list of the bids made on the auction
     */
    fun get(auction: RPKAuction): List<RPKBid> {
        if (auctionCache.containsKey(auction.id)) {
            return auctionCache.get(auction.id) as List<RPKBid>
        } else {
            val bids = mutableListOf<RPKBid>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM rpkit_bid WHERE auction_id = ?"
                ).use { statement ->
                    statement.setInt(1, auction.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val bid = get(resultSet.getInt("id"))
                        if (bid != null) {
                            bids.add(bid)
                        }
                    }
                }
            }
            return bids
        }
    }

    override fun delete(entity: RPKBid) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_bid WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                val bids = auctionCache.get(entity.auction.id) as MutableList<RPKBid>
                bids.remove(entity)
                auctionCache.put(entity.auction.id, bids)
            }
        }
    }
}