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

package com.seventh_root.elysium.shops.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.shopcount.ElysiumShopCount
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS


class ElysiumShopCountTable: Table<ElysiumShopCount> {

    private val plugin: ElysiumShopsBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumShopCount>
    private val characterCache: Cache<Int, Int>

    constructor(database: Database, plugin: ElysiumShopsBukkit): super(database, ElysiumShopCount::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumShopCount::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        characterCache = cacheManager.createCache("characterCache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_shop_count(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "count INTEGER," +
                            "FOREIGN KEY(character_id) REFERENCES elysium_character(id)" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun insert(entity: ElysiumShopCount): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_shop_count(character_id, count) VALUES(?, ?)",
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

    override fun update(entity: ElysiumShopCount) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_shop_count SET character_id = ?, count = ? WHERE id = ?"
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

    override fun get(id: Int): ElysiumShopCount? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var shopCount: ElysiumShopCount? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, count FROM elysium_shop_count WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val characterId = resultSet.getInt("character_id")
                        shopCount = ElysiumShopCount(
                                id,
                                plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
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

    fun get(character: ElysiumCharacter): ElysiumShopCount? {
        if (characterCache.containsKey(character.id)) {
            return get(characterCache.get(character.id) as Int)
        } else {
            var shopCount: ElysiumShopCount? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, count FROM elysium_shop_count WHERE character_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        shopCount = ElysiumShopCount(
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

    override fun delete(entity: ElysiumShopCount) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_shop_count WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                characterCache.remove(entity.character.id)
            }
        }
    }
}