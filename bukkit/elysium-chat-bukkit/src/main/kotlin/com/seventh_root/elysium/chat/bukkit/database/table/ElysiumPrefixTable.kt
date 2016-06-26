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

import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefix
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixImpl
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS


class ElysiumPrefixTable: Table<ElysiumPrefix> {

    val cacheManager: CacheManager
    val cache: Cache<Int, ElysiumPrefix>
    val nameCache: Cache<String, ElysiumPrefix>

    constructor(database: Database): super(database, ElysiumPrefix::class) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumPrefix::class.java,
                        ResourcePoolsBuilder.heap(10L)).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, ElysiumPrefix::class.java,
                        ResourcePoolsBuilder.heap(10L).build()).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_prefix(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)," +
                            "prefix VARCHAR(256)" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.3.0")
        }
    }

    override fun insert(`object`: ElysiumPrefix): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_prefix(name, prefix) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, `object`.name)
                statement.setString(2, `object`.prefix)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    nameCache.put(`object`.name, `object`)
                }
            }
        }
        return id
    }

    override fun update(`object`: ElysiumPrefix) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_prefix SET name = ?, prefix = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, `object`.name)
                statement.setString(2, `object`.prefix)
                statement.setInt(3, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                nameCache.put(`object`.name, `object`)
            }
        }
    }

    override fun get(id: Int): ElysiumPrefix? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var prefix: ElysiumPrefix? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, prefix FROM elysium_prefix WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalPrefix = ElysiumPrefixImpl(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("prefix")
                        )
                        prefix = finalPrefix
                        cache.put(finalPrefix.id, finalPrefix)
                        nameCache.put(finalPrefix.name, finalPrefix)
                    }
                }
            }
            return prefix
        }
    }

    fun get(name: String): ElysiumPrefix? {
        if (nameCache.containsKey(name)) {
            return nameCache.get(name)
        } else {
            var prefix: ElysiumPrefix? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, prefix FROM elysium_prefix WHERE name = ?"
                ).use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalPrefix = ElysiumPrefixImpl(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("prefix")
                        )
                        prefix = finalPrefix
                        cache.put(finalPrefix.id, finalPrefix)
                        nameCache.put(finalPrefix.name, finalPrefix)
                    }
                }
            }
            return prefix
        }
    }

    fun getAll(): List<ElysiumPrefix> {
        val prefixes = mutableListOf<ElysiumPrefix>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_prefix"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val prefix = get(resultSet.getInt("id"))
                    if (prefix != null) {
                        prefixes.add(prefix)
                    }
                }
            }
        }
        return prefixes
    }

    override fun delete(`object`: ElysiumPrefix) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_prefix WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.id)
                statement.executeUpdate()
                cache.remove(`object`.id)
                nameCache.remove(`object`.name)
            }
        }
    }

}