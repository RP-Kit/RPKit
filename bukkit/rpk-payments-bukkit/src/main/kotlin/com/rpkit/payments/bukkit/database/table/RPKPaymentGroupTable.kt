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

package com.rpkit.payments.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupImpl
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.INTEGER

/**
 * Represents payment group table.
 */
class RPKPaymentGroupTable(database: Database, private val plugin: RPKPaymentsBukkit): Table<RPKPaymentGroup>(database, RPKPaymentGroup::class) {

    val cacheManager: CacheManager
    val cache: Cache<Int, RPKPaymentGroup>
    val nameCache: Cache<String, RPKPaymentGroup>

    init {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPaymentGroup::class.java, ResourcePoolsBuilder.heap(20L)))
        nameCache = cacheManager.createCache("nameCache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String::class.java, RPKPaymentGroup::class.java, ResourcePoolsBuilder.heap(20L)))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `rpkit_payment_group`(" +
                    "`id` INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "`name` VARCHAR(256), " +
                    "`amount` INTEGER, " +
                    "`currency_id` INTEGER, " +
                    "`interval` BIGINT, " +
                    "`last_payment_time` DATETIME, " +
                    "`balance` INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKPaymentGroup): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO `rpkit_payment_group`(" +
                            "`name`, " +
                            "`amount`, " +
                            "`currency_id`, " +
                            "`interval`, " +
                            "`last_payment_time`, " +
                            "`balance`" +
                    ") VALUES(?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setInt(2, entity.amount)
                val currency = entity.currency
                if (currency != null) {
                    statement.setInt(3, currency.id)
                } else {
                    statement.setNull(3, INTEGER)
                }
                statement.setLong(4, entity.interval)
                statement.setDate(5, Date(entity.lastPaymentTime))
                statement.setInt(6, entity.balance)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    nameCache.put(entity.name, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKPaymentGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE `rpkit_payment_group` SET " +
                            "`name` = ?, " +
                            "`amount` = ?," +
                            "`currency_id` = ?, " +
                            "`interval` = ?, " +
                            "`last_payment_time` = ?, " +
                            "`balance` = ?" +
                            " WHERE `id` = ?"
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setInt(2, entity.amount)
                val currency = entity.currency
                if (currency != null) {
                    statement.setInt(3, currency.id)
                } else {
                    statement.setNull(3, INTEGER)
                }
                statement.setLong(4, entity.interval)
                statement.setDate(5, Date(entity.lastPaymentTime))
                statement.setInt(6, entity.balance)
                statement.setInt(7, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                nameCache.put(entity.name, entity)
            }
        }
    }

    override fun get(id: Int): RPKPaymentGroup? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            var paymentGroup: RPKPaymentGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT `id`, `name`, `amount`, `currency_id`, `interval`, `last_payment_time`, `balance`" +
                                " FROM `rpkit_payment_group`" +
                                " WHERE `id` = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                        val currencyId = resultSet.getInt("currency_id")
                        val currency = if (currencyId == 0) null else currencyProvider.getCurrency(currencyId)
                        val finalPaymentGroup = RPKPaymentGroupImpl(
                                plugin,
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getInt("amount"),
                                currency,
                                resultSet.getLong("interval"),
                                resultSet.getDate("last_payment_time").time,
                                resultSet.getInt("balance")
                        )
                        cache.put(id, finalPaymentGroup)
                        nameCache.put(finalPaymentGroup.name, finalPaymentGroup)
                        paymentGroup = finalPaymentGroup
                    }
                }
            }
            return paymentGroup
        }
    }

    fun get(name: String): RPKPaymentGroup? {
        if (nameCache.containsKey(name)) {
            return nameCache[name]
        } else {
            var paymentGroup: RPKPaymentGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT `id`, `name`, `amount`, `currency_id`, `interval`, `last_payment_time`, `balance`" +
                                " FROM `rpkit_payment_group`" +
                                " WHERE `name` = ?"
                ).use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                        val currencyId = resultSet.getInt("currency_id")
                        val currency = if (currencyId == 0) null else currencyProvider.getCurrency(currencyId)
                        val finalPaymentGroup = RPKPaymentGroupImpl(
                                plugin,
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getInt("amount"),
                                currency,
                                resultSet.getLong("interval"),
                                resultSet.getDate("last_payment_time").time,
                                resultSet.getInt("balance")
                        )
                        cache.put(finalPaymentGroup.id, finalPaymentGroup)
                        nameCache.put(name, finalPaymentGroup)
                        paymentGroup = finalPaymentGroup
                    }
                }
            }
            return paymentGroup
        }
    }

    fun getAll(): List<RPKPaymentGroup> {
        val paymentGroups = mutableListOf<RPKPaymentGroup>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT `id` FROM `rpkit_payment_group`"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    paymentGroups.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return paymentGroups
    }

    override fun delete(entity: RPKPaymentGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement("DELETE FROM `rpkit_payment_group` WHERE `id` = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                nameCache.remove(entity.name)
            }
        }
    }

}