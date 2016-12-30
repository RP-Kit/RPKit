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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.gender.RPKGender
import com.rpkit.characters.bukkit.gender.RPKGenderImpl
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents the gender table.
 */
class RPKGenderTable: Table<RPKGender> {

    private val cacheManager: CacheManager
    private val cache: Cache<Int, RPKGender>
    private val nameCache: Cache<String, Int>

    constructor(database: Database): super(database, RPKGender::class.java) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKGender::class.java,
                        ResourcePoolsBuilder.heap(10L)).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(10L)).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS rpkit_gender(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)" +
                        ")").use { statement ->
                    statement.executeUpdate()
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(entity: RPKGender): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO rpkit_gender(name) VALUES(?)",
                        RETURN_GENERATED_KEYS).use { statement ->
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
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return 0
    }

    override fun update(entity: RPKGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE rpkit_gender SET name = ? WHERE id = ?").use { statement ->
                    statement.setString(1, entity.name)
                    statement.setInt(2, entity.id)
                    statement.executeUpdate()
                    cache.put(entity.id, entity)
                    nameCache.put(entity.name, entity.id)
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): RPKGender? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            try {
                var gender: RPKGender? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM rpkit_gender WHERE id = ?").use { statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id1 = resultSet.getInt("id")
                            val name = resultSet.getString("name")
                            gender = RPKGenderImpl(id1, name)
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

    /**
     * Gets a gender by name.
     * If no gender is found with the given name, null is returned.
     *
     * @param name The name of the gender
     * @return The gender, or null if no gender is found with the given name
     */
    operator fun get(name: String): RPKGender? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            try {
                var gender: RPKGender? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM rpkit_gender WHERE name = ?").use { statement ->
                        statement.setString(1, name)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id = resultSet.getInt("id")
                            val name1 = resultSet.getString("name")
                            gender = RPKGenderImpl(id, name1)
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

    override fun delete(entity: RPKGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM rpkit_gender WHERE id = ?").use { statement ->
                    statement.setInt(1, entity.id)
                    statement.executeUpdate()
                    cache.remove(entity.id)
                    nameCache.remove(entity.name)
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
