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

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.group.ElysiumPaymentGroup
import com.seventh_root.elysium.payments.bukkit.group.invite.ElysiumPaymentGroupInvite
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents payment group invite table.
 */
class ElysiumPaymentGroupInviteTable(
        database: Database,
        private val plugin: ElysiumPaymentsBukkit
): Table<ElysiumPaymentGroupInvite>(database, ElysiumPaymentGroupInvite::class) {

    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumPaymentGroupInvite>

    init {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumPaymentGroupInvite::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong() * 20)))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_payment_group_invite(" +
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

    override fun insert(entity: ElysiumPaymentGroupInvite): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_payment_group_invite(payment_group_id, character_id) VALUES(?, ?)",
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

    override fun update(entity: ElysiumPaymentGroupInvite) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_payment_group_invite SET" +
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

    override fun get(id: Int): ElysiumPaymentGroupInvite? {
        var paymentGroupInvite: ElysiumPaymentGroupInvite? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, payment_group_id, character_id FROM elysium_payment_group_invite WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val paymentGroupTable = plugin.core.database.getTable(ElysiumPaymentGroupTable::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                    val finalPaymentGroupInvite = ElysiumPaymentGroupInvite(
                            resultSet.getInt("id"),
                            paymentGroupTable[resultSet.getInt("payment_group_id")]!!,
                            characterProvider.getCharacter(resultSet.getInt("character_id"))!!
                    )
                    cache.put(id, finalPaymentGroupInvite)
                    paymentGroupInvite = finalPaymentGroupInvite
                }
            }
        }
        return paymentGroupInvite
    }

    fun get(paymentGroup: ElysiumPaymentGroup): List<ElysiumPaymentGroupInvite> {
        val paymentGroupInvites = mutableListOf<ElysiumPaymentGroupInvite>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_payment_group_invite WHERE payment_group_id = ?"
            ).use { statement ->
                statement.setInt(1, paymentGroup.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    paymentGroupInvites.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return paymentGroupInvites
    }

    override fun delete(entity: ElysiumPaymentGroupInvite) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_payment_group_invite WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}