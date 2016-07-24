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

package com.seventh_root.elysium.chat.bukkit.database.table

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroup
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroupImpl
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS


class ElysiumChatGroupTable: Table<ElysiumChatGroup> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumChatGroup>
    private val nameCache: Cache<String, Int>

    constructor(database: Database, plugin: ElysiumChatBukkit): super(database, ElysiumChatGroup::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumChatGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_chat_group(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)" +
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

    override fun insert(entity: ElysiumChatGroup): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_chat_group(name) VALUES(?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.name)
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

    override fun update(entity: ElysiumChatGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_chat_group SET name = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setInt(2, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                nameCache.put(entity.name, entity.id)
            }
        }
    }

    override fun get(id: Int): ElysiumChatGroup? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatGroup: ElysiumChatGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name FROM elysium_chat_group WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatGroup = ElysiumChatGroupImpl(
                                plugin,
                                resultSet.getInt("id"),
                                resultSet.getString("name")
                        )
                        chatGroup = finalChatGroup
                        cache.put(finalChatGroup.id, finalChatGroup)
                        nameCache.put(finalChatGroup.name, finalChatGroup.id)
                    }
                }
            }
            return chatGroup
        }
    }

    fun get(name: String): ElysiumChatGroup? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name))
        } else {
            var chatGroup: ElysiumChatGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name FROM elysium_chat_group WHERE name = ?"
                ).use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatGroup = ElysiumChatGroupImpl(
                                plugin,
                                resultSet.getInt("id"),
                                resultSet.getString("name")
                        )
                        chatGroup = finalChatGroup
                        cache.put(finalChatGroup.id, finalChatGroup)
                        nameCache.put(finalChatGroup.name, finalChatGroup.id)
                    }
                }
            }
            return chatGroup
        }
    }

    override fun delete(entity: ElysiumChatGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_chat_group WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                nameCache.remove(entity.name)
            }
        }
    }
}