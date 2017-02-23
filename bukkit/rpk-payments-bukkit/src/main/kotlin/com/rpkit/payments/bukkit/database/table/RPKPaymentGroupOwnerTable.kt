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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProvider
import com.rpkit.payments.bukkit.group.owner.RPKPaymentGroupOwner
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents payment group owner table.
 */
class RPKPaymentGroupOwnerTable(
        database: Database,
        private val plugin: RPKPaymentsBukkit
): Table<RPKPaymentGroupOwner>(database, RPKPaymentGroupOwner::class) {

    private val cacheManager: CacheManager
    private val cache: Cache<Int, RPKPaymentGroupOwner>

    init {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPaymentGroupOwner::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong() * 20)))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_payment_group_owner(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "payment_group_id INTEGER," +
                            "character_id INTEGER" +
                            ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKPaymentGroupOwner): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_payment_group_owner(payment_group_id, character_id) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.paymentGroup.id)
                statement.setInt(2, entity.character.id)
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

    override fun update(entity: RPKPaymentGroupOwner) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_payment_group_owner SET" +
                            "payment_group_id = ?, " +
                            "character_id = ? " +
                            "WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.paymentGroup.id)
                statement.setInt(2, entity.character.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKPaymentGroupOwner? {
        var paymentGroupOwner: RPKPaymentGroupOwner? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, payment_group_id, character_id FROM rpkit_payment_group_owner WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val paymentGroup = paymentGroupProvider.getPaymentGroup(resultSet.getInt("payment_group_id"))
                    val character = characterProvider.getCharacter(resultSet.getInt("character_id"))
                    if (paymentGroup == null || character == null) {
                        connection.prepareStatement("DELETE FROM rpkit_payment_group_owner WHERE id = ?").use { statement ->
                            statement.setInt(1, resultSet.getInt("id"))
                            statement.executeUpdate()
                        }
                    } else {
                        val finalPaymentGroupOwner = RPKPaymentGroupOwner(
                                resultSet.getInt("id"),
                                paymentGroup,
                                character
                        )
                        cache.put(id, finalPaymentGroupOwner)
                        paymentGroupOwner = finalPaymentGroupOwner
                    }
                }
            }
        }
        return paymentGroupOwner
    }

    fun get(paymentGroup: RPKPaymentGroup): List<RPKPaymentGroupOwner> {
        val paymentGroupOwners = mutableListOf<RPKPaymentGroupOwner>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_payment_group_owner WHERE payment_group_id = ?"
            ).use { statement ->
                statement.setInt(1, paymentGroup.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    paymentGroupOwners.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return paymentGroupOwners
    }

    override fun delete(entity: RPKPaymentGroupOwner) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_payment_group_owner WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}