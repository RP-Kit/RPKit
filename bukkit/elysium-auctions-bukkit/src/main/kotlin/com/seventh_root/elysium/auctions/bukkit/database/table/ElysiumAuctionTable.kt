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

package com.seventh_root.elysium.auctions.bukkit.database.table

import com.seventh_root.elysium.auctions.bukkit.ElysiumAuctionsBukkit
import com.seventh_root.elysium.auctions.bukkit.auction.ElysiumAuction
import com.seventh_root.elysium.auctions.bukkit.auction.ElysiumAuctionImpl
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.bukkit.util.itemStackFromByteArray
import com.seventh_root.elysium.core.bukkit.util.toByteArray
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import org.bukkit.Location
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.*

/**
 * Represents the auction table.
 */
class ElysiumAuctionTable: Table<ElysiumAuction> {

    private val plugin: ElysiumAuctionsBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumAuction>

    constructor(database: Database, plugin: ElysiumAuctionsBukkit): super(database, ElysiumAuction::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumAuction::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_auction(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "item BLOB," +
                            "currency_id INTEGER," +
                            "world VARCHAR(256)," +
                            "x DOUBLE," +
                            "y DOUBLE," +
                            "z DOUBLE," +
                            "yaw REAL," +
                            "pitch REAL," +
                            "character_id INTEGER," +
                            "duration BIGINT," +
                            "end_time BIGINT," +
                            "start_price INTEGER," +
                            "buy_out_price INTEGER," +
                            "no_sell_price INTEGER," +
                            "minimum_bid_increment INTEGER," +
                            "bidding_open BOOLEAN" +
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

    override fun insert(entity: ElysiumAuction): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_auction(item, currency_id, world, x, y, z, yaw, pitch, character_id, duration, end_time, start_price, buy_out_price, no_sell_price, minimum_bid_increment, bidding_open) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setBytes(1, entity.item.toByteArray())
                statement.setInt(2, entity.currency.id)
                val location = entity.location
                if (location != null) {
                    statement.setString(3, location.world.name)
                    statement.setDouble(4, location.x)
                    statement.setDouble(5, location.y)
                    statement.setDouble(6, location.z)
                    statement.setFloat(7, location.yaw)
                    statement.setFloat(8, location.pitch)
                } else {
                    statement.setNull(3, VARCHAR)
                    statement.setNull(4, DOUBLE)
                    statement.setNull(5, DOUBLE)
                    statement.setNull(6, DOUBLE)
                    statement.setNull(7, FLOAT)
                    statement.setNull(8, FLOAT)
                }
                statement.setInt(9, entity.character.id)
                statement.setLong(10, entity.duration)
                statement.setLong(11, entity.endTime)
                statement.setInt(12, entity.startPrice)
                val buyOutPrice = entity.buyOutPrice
                if (buyOutPrice != null) {
                    statement.setInt(13, buyOutPrice)
                } else {
                    statement.setNull(13, INTEGER)
                }
                val noSellPrice = entity.noSellPrice
                if (noSellPrice != null) {
                    statement.setInt(14, noSellPrice)
                } else {
                    statement.setNull(14, INTEGER)
                }
                statement.setInt(15, entity.minimumBidIncrement)
                statement.setBoolean(16, entity.isBiddingOpen)
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

    override fun update(entity: ElysiumAuction) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_auction SET item = ?, currency_id = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, character_id = ?, duration = ?, end_time = ?, start_price = ?, buy_out_price = ?, no_sell_price = ?, minimum_bid_increment = ?, bidding_open = ? WHERE id = ?"
            ).use { statement ->
                statement.setBytes(1, entity.item.toByteArray())
                statement.setInt(2, entity.currency.id)
                val location = entity.location
                if (location != null) {
                    statement.setString(3, location.world.name)
                    statement.setDouble(4, location.x)
                    statement.setDouble(5, location.y)
                    statement.setDouble(6, location.z)
                    statement.setFloat(7, location.yaw)
                    statement.setFloat(8, location.pitch)
                } else {
                    statement.setNull(3, VARCHAR)
                    statement.setNull(4, DOUBLE)
                    statement.setNull(5, DOUBLE)
                    statement.setNull(6, DOUBLE)
                    statement.setNull(7, FLOAT)
                    statement.setNull(8, FLOAT)
                }
                statement.setInt(9, entity.character.id)
                statement.setLong(10, entity.duration)
                statement.setLong(11, entity.endTime)
                statement.setInt(12, entity.startPrice)
                val buyOutPrice = entity.buyOutPrice
                if (buyOutPrice != null) {
                    statement.setInt(13, buyOutPrice)
                } else {
                    statement.setNull(13, INTEGER)
                }
                val noSellPrice = entity.noSellPrice
                if (noSellPrice != null) {
                    statement.setInt(14, noSellPrice)
                } else {
                    statement.setNull(14, INTEGER)
                }
                statement.setInt(15, entity.minimumBidIncrement)
                statement.setBoolean(16, entity.isBiddingOpen)
                statement.setInt(17, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): ElysiumAuction? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var auction: ElysiumAuction? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, item, currency_id, world, x, y, z, yaw, pitch, character_id, duration, end_time, start_price, buy_out_price, no_sell_price, minimum_bid_increment, bidding_open FROM elysium_auction WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalAuction = ElysiumAuctionImpl(
                                plugin,
                                resultSet.getInt("id"),
                                itemStackFromByteArray(resultSet.getBytes("item")),
                                plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).getCurrency(resultSet.getInt("currency_id"))!!,
                                Location(
                                        plugin.server.getWorld(resultSet.getString("world")),
                                        resultSet.getDouble("x"),
                                        resultSet.getDouble("y"),
                                        resultSet.getDouble("z"),
                                        resultSet.getFloat("yaw"),
                                        resultSet.getFloat("pitch")
                                ),
                                plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                                resultSet.getLong("duration"),
                                resultSet.getLong("end_time"),
                                resultSet.getInt("start_price"),
                                resultSet.getInt("buy_out_price"),
                                resultSet.getInt("no_sell_price"),
                                resultSet.getInt("minimum_bid_increment"),
                                resultSet.getBoolean("bidding_open")
                        )
                        cache.put(finalAuction.id, finalAuction)
                        auction = finalAuction
                    }
                }
            }
            return auction
        }
    }

    /**
     * Gets a list of all auctions
     *
     * @return A list containing all auctions
     */
    fun getAll(): List<ElysiumAuction> {
        val auctions = mutableListOf<ElysiumAuction>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_auction"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val auction = get(resultSet.getInt("id"))
                    if (auction != null) {
                        auctions.add(auction)
                    }
                }
            }
        }
        return auctions
    }

    override fun delete(entity: ElysiumAuction) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_auction WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
        for (bid in entity.bids) {
            database.getTable(ElysiumBidTable::class).delete(bid)
        }
    }

}