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

package com.seventh_root.elysium.payments.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.group.ElysiumPaymentGroupProvider
import com.seventh_root.elysium.payments.bukkit.notification.ElysiumPaymentNotification
import com.seventh_root.elysium.payments.bukkit.notification.ElysiumPaymentNotificationImpl
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents payment notification table.
 */
class ElysiumPaymentNotificationTable(
        database: Database,
        private val plugin: ElysiumPaymentsBukkit
): Table<ElysiumPaymentNotification>(database, ElysiumPaymentNotification::class) {

    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumPaymentNotification>

    init {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumPaymentNotification::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_payment_notification(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "group_id INTEGER," +
                            "to_id INTEGER," +
                            "character_id INTEGER," +
                            "date DATE," +
                            "text VARCHAR(1024)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: ElysiumPaymentNotification): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement("INSERT INTO elysium_payment_notification(" +
                    "group_id, " +
                    "to_id, " +
                    "character_id, " +
                    "date," +
                    "text" +
                    ") VALUES(?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                statement.setInt(1, entity.group.id)
                statement.setInt(2, entity.to.id)
                statement.setInt(3, entity.character.id)
                statement.setDate(4, Date(entity.date))
                statement.setString(5, entity.text)
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

    override fun update(entity: ElysiumPaymentNotification) {
        database.createConnection().use { connection ->
            connection.prepareStatement("UPDATE elysium_payment_notification SET " +
                    "group_id = ?, " +
                    "to_id = ?, " +
                    "character_id = ?, " +
                    "date = ?, " +
                    "text = ?" +
                    "WHERE id = ?").use { statement ->
                statement.setInt(1, entity.group.id)
                statement.setInt(2, entity.to.id)
                statement.setInt(3, entity.character.id)
                statement.setDate(4, Date(entity.date))
                statement.setString(5, entity.text)
                statement.setInt(6, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): ElysiumPaymentNotification? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var paymentNotification: ElysiumPaymentNotification? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, group_id, to_id, character_id, date, text FROM elysium_payment_notification WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val groupProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPaymentGroupProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                        val finalPaymentNotification = ElysiumPaymentNotificationImpl(
                                resultSet.getInt("id"),
                                groupProvider.getPaymentGroup(resultSet.getInt("group_id"))!!,
                                characterProvider.getCharacter(resultSet.getInt("to_id"))!!,
                                characterProvider.getCharacter(resultSet.getInt("character_id"))!!,
                                resultSet.getDate("date").time,
                                resultSet.getString("text")
                        )
                        cache.put(finalPaymentNotification.id, finalPaymentNotification)
                        paymentNotification = finalPaymentNotification
                    }
                }
            }
            return paymentNotification
        }
    }

    fun getAll(): List<ElysiumPaymentNotification> {
        val paymentNotifications = mutableListOf<ElysiumPaymentNotification>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_payment_notification"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    paymentNotifications.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return paymentNotifications
    }

    fun get(character: ElysiumCharacter): List<ElysiumPaymentNotification> {
        val paymentNotifications = mutableListOf<ElysiumPaymentNotification>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_payment_notification WHERE character_id = ?"
            ).use { statement ->
                statement.setInt(1, character.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    paymentNotifications.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return paymentNotifications
    }

    override fun delete(entity: ElysiumPaymentNotification) {
        database.createConnection().use { connection ->
            connection.prepareStatement("DELETE FROM elysium_payment_notification WHERE id = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}