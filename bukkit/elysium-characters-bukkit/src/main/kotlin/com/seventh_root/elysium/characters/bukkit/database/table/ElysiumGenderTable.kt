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

package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGender
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderImpl
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class ElysiumGenderTable: Table<ElysiumGender> {

    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumGender>
    private val nameCache: Cache<String, Int>

    constructor(database: Database): super(database, ElysiumGender::class.java) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumGender::class.java,
                        ResourcePoolsBuilder.heap(10L)).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(10L)).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS elysium_gender(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)" +
                        ")").use { statement ->
                    statement.executeUpdate()
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(`object`: ElysiumGender): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO elysium_gender(name) VALUES(?)",
                        RETURN_GENERATED_KEYS).use { statement ->
                    statement.setString(1, `object`.name)
                    statement.executeUpdate()
                    val generatedKeys = statement.generatedKeys
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1)
                        `object`.id = id
                        cache.put(id, `object`)
                        nameCache.put(`object`.name, id)
                    }
                }
            }
            return id
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return 0
    }

    override fun update(`object`: ElysiumGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE elysium_gender SET name = ? WHERE id = ?").use { statement ->
                    statement.setString(1, `object`.name)
                    statement.setInt(2, `object`.id)
                    statement.executeUpdate()
                    cache.put(`object`.id, `object`)
                    nameCache.put(`object`.name, `object`.id)
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): ElysiumGender? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            try {
                var gender: ElysiumGender? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM elysium_gender WHERE id = ?").use { statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id1 = resultSet.getInt("id")
                            val name = resultSet.getString("name")
                            gender = ElysiumGenderImpl(id1, name)
                            cache.put(id, gender)
                            nameCache.put(name, id1)
                        }
                    }
                }
                return gender
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    operator fun get(name: String): ElysiumGender? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            try {
                var gender: ElysiumGender? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM elysium_gender WHERE name = ?").use { statement ->
                        statement.setString(1, name)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id = resultSet.getInt("id")
                            val name1 = resultSet.getString("name")
                            gender = ElysiumGenderImpl(id, name1)
                            cache.put(id, gender)
                            nameCache.put(name1, id)
                        }
                    }
                }
                return gender
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    override fun delete(`object`: ElysiumGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM elysium_gender WHERE id = ?").use { statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                    cache.remove(`object`.id)
                    nameCache.remove(`object`.name)
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
