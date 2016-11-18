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

package com.seventh_root.elysium.economy.bukkit.database.table

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyImpl
import org.bukkit.Material
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*

/**
 * Represents the currency table.
 */
class ElysiumCurrencyTable: Table<ElysiumCurrency> {

    private val plugin: ElysiumEconomyBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumCurrency>
    private val nameCache: Cache<String, Int>

    constructor(database: Database, plugin: ElysiumEconomyBukkit): super(database, ElysiumCurrency::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumCurrency::class.java,
                        ResourcePoolsBuilder.heap(5L)).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(5L)).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_currency(" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(256)," +
                        "name_singular VARCHAR(256)," +
                        "name_plural VARCHAR(256)," +
                        "rate DOUBLE," +
                        "default_amount INTEGER," +
                        "material VARCHAR(256)" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(entity: ElysiumCurrency): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_currency(name, name_singular, name_plural, rate, default_amount, material) VALUES(?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setString(2, entity.nameSingular)
                statement.setString(3, entity.namePlural)
                statement.setDouble(4, entity.rate)
                statement.setInt(5, entity.defaultAmount)
                statement.setString(6, entity.material.name)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    nameCache.put(entity.name, id)
                }
            }
        }
        return id
    }

    override fun update(entity: ElysiumCurrency) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_currency SET name = ?, name_singular = ?, name_plural = ?, rate = ?, default_amount = ?, material = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setString(2, entity.nameSingular)
                statement.setString(3, entity.namePlural)
                statement.setDouble(4, entity.rate)
                statement.setInt(5, entity.defaultAmount)
                statement.setString(6, entity.material.name)
                statement.setInt(7, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                nameCache.put(entity.name, entity.id)
            }
        }
    }

    override fun get(id: Int): ElysiumCurrency? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var currency: ElysiumCurrency? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, name_singular, name_plural, rate, default_amount, material FROM elysium_currency WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        currency = ElysiumCurrencyImpl(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("name_singular"),
                                resultSet.getString("name_plural"),
                                resultSet.getDouble("rate"),
                                resultSet.getInt("default_amount"),
                                Material.getMaterial(resultSet.getString("material"))
                        )
                        if (currency != null) {
                            val finalCurrency = currency!!
                            cache.put(id, finalCurrency)
                            nameCache.put(finalCurrency.name, id)
                        }
                    }
                }
            }
            return currency
        }
    }

    /**
     * Gets a currency by name.
     * If no currency is found with the given name is found, null is returned.
     *
     * @param name The name
     * @return The currency, or null if no currency is found with the given name
     */
    fun get(name: String): ElysiumCurrency? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            var currency: ElysiumCurrency? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, name_singular, name_plural, rate, default_amount, material FROM elysium_currency WHERE name = ?"
                ).use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalCurrency = ElysiumCurrencyImpl(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("name_singular"),
                                resultSet.getString("name_plural"),
                                resultSet.getDouble("rate"),
                                resultSet.getInt("default_amount"),
                                Material.getMaterial(resultSet.getString("material"))
                        )
                        currency = finalCurrency
                        cache.put(finalCurrency.id, finalCurrency)
                        nameCache.put(finalCurrency.name, finalCurrency.id)
                    }
                }
            }
            return currency
        }
    }

    /**
     * Gets all currencies contained in the table.
     *
     * @return A collection containing all currencies.
     */
    fun getAll(): Collection<ElysiumCurrency> {
        val currencies = ArrayList<ElysiumCurrency>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_currency"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val currency = get(resultSet.getInt("id"))
                    if (currency != null) currencies.add(currency)
                }
            }
        }
        return currencies
    }

    override fun delete(entity: ElysiumCurrency) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_currency WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                if (cache.containsKey(entity.id)) {
                    cache.remove(entity.id)
                }
            }
        }
    }

}