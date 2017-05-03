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

package com.rpkit.chat.bukkit.database.table

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.snooper.RPKSnooper
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents the snooper table.
 */
class RPKSnooperTable: Table<RPKSnooper> {

    private val plugin: RPKChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, RPKSnooper>

    constructor(database: Database, plugin: RPKChatBukkit) : super(database, RPKSnooper::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKSnooper::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.3.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "TRUNCATE rpkit_snooper"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE rpkit_snooper " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN minecraft_profile_id INTEGER"
                )
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_snooper(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "minecraft_profile_id INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun insert(entity: RPKSnooper): Int {
        var id: Int = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_snooper(minecraft_profile_id) VALUES(?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
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

    override fun update(entity: RPKSnooper) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_snooper SET player_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setInt(2, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKSnooper? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var snooper: RPKSnooper? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, player_id FROM rpkit_snooper WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalSnooper = RPKSnooper(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))!!
                        )
                        snooper = finalSnooper
                        cache.put(id, finalSnooper)
                    }
                }
            }
            return snooper
        }
    }

    /**
     * Gets the snooper instance for a Minecraft profile.
     * If the player does not have a snooper entry, null is returned.
     *
     * @param minecraftProfile The player
     * @return The snooper instance, or null if none exists
     */
    fun get(minecraftProfile: RPKMinecraftProfile): RPKSnooper? {
        var snooper: RPKSnooper? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_snooper WHERE minecraft_profile_id = ?"
            ).use { statement ->
                statement.setInt(1, minecraftProfile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    snooper = get(resultSet.getInt("id"))
                }
            }
        }
        return snooper
    }

    /**
     * Gets all snoopers
     *
     * @return A list containing all snoopers
     */
    fun getAll(): List<RPKSnooper> {
        val snoopers = mutableListOf<RPKSnooper>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_snooper"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val snooper = get(resultSet.getInt("id"))
                    if (snooper != null) {
                        snoopers.add(snooper)
                    }
                }
            }
        }
        return snoopers
    }

    override fun delete(entity: RPKSnooper) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_snooper WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}