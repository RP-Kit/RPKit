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

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.character.MoneyHidden
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS

class MoneyHiddenTable: Table<MoneyHidden> {

    private val plugin: ElysiumEconomyBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, MoneyHidden>
    private val characterCache: Cache<Int, Int>

    constructor(database: Database, plugin: ElysiumEconomyBukkit): super(database, MoneyHidden::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MoneyHidden::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        characterCache = cacheManager.createCache("characterCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS money_hidden(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
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

    override fun insert(entity: MoneyHidden): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO money_hidden(character_id) VALUES(?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    characterCache.put(entity.character.id, id)
                }
            }
        }
        return id
    }

    override fun update(entity: MoneyHidden) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE money_hidden SET character_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                characterCache.put(entity.character.id, entity.id)
            }
        }
    }

    override fun get(id: Int): MoneyHidden? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var moneyHidden: MoneyHidden? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id FROM money_hidden WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalMoneyHidden = MoneyHidden(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!
                        )
                        moneyHidden = finalMoneyHidden
                        cache.put(finalMoneyHidden.id, finalMoneyHidden)
                        characterCache.put(finalMoneyHidden.character.id, finalMoneyHidden.id)
                    }
                }
            }
            return moneyHidden
        }
    }

    fun get(character: ElysiumCharacter): MoneyHidden? {
        if (cache.containsKey(character.id)) {
            return get(characterCache.get(character.id))
        } else {
            var moneyHidden: MoneyHidden? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id FROM money_hidden WHERE character_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalMoneyHidden = MoneyHidden(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!
                        )
                        moneyHidden = finalMoneyHidden
                        cache.put(finalMoneyHidden.id, finalMoneyHidden)
                        characterCache.put(finalMoneyHidden.character.id, finalMoneyHidden.id)
                    }
                }
            }
            return moneyHidden
        }
    }

    override fun delete(entity: MoneyHidden) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM money_hidden WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                characterCache.remove(entity.character.id)
            }
        }
    }

}
