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

package com.rpkit.shops.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.shopcount.RPKShopCount
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents the shop count table.
 */
class RPKShopCountTable: Table<RPKShopCount> {

    private val plugin: RPKShopsBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, RPKShopCount>
    private val characterCache: Cache<Int, Int>

    constructor(database: Database, plugin: RPKShopsBukkit): super(database, RPKShopCount::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKShopCount::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        characterCache = cacheManager.createCache("characterCache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_shop_count(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "count INTEGER" +
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

    override fun insert(entity: RPKShopCount): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_shop_count(character_id, count) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.count)
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

    override fun update(entity: RPKShopCount) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_shop_count SET character_id = ?, count = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.count)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                characterCache.put(entity.character.id, entity.id)
            }
        }
    }

    override fun get(id: Int): RPKShopCount? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var shopCount: RPKShopCount? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, count FROM rpkit_shop_count WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val characterId = resultSet.getInt("character_id")
                        shopCount = RPKShopCount(
                                id,
                                plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                                        .getCharacter(characterId)!!,
                                resultSet.getInt("count")
                        )
                        cache.put(id, shopCount)
                        characterCache.put(characterId, id)
                    }
                }
            }
            return shopCount
        }
    }

    /**
     * Gets the shop count for a character.
     * If there is no shop count for the given character
     *
     * @param character The character
     * @return The shop count for the character, or null if there is no shop count for the given character
     */
    fun get(character: RPKCharacter): RPKShopCount? {
        if (characterCache.containsKey(character.id)) {
            return get(characterCache.get(character.id) as Int)
        } else {
            var shopCount: RPKShopCount? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, count FROM rpkit_shop_count WHERE character_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        shopCount = RPKShopCount(
                                id,
                                character,
                                resultSet.getInt("count")
                        )
                        cache.put(id, shopCount)
                        characterCache.put(character.id, id)
                    }
                }
            }
            return shopCount
        }
    }

    override fun delete(entity: RPKShopCount) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_shop_count WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                characterCache.remove(entity.character.id)
            }
        }
    }
}