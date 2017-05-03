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
import com.rpkit.chat.bukkit.chatgroup.LastUsedChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
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
 * Represents the last used chat group table
 */
class LastUsedChatGroupTable: Table<LastUsedChatGroup> {

    private val plugin: RPKChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, LastUsedChatGroup>
    private val minecraftProfileCache: Cache<Int, Int>

    constructor(database: Database, plugin: RPKChatBukkit): super(database, LastUsedChatGroup::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, LastUsedChatGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
        minecraftProfileCache = cacheManager.createCache("minecraftProfileCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS last_used_chat_group(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "minecraft_profile_id INTEGER," +
                            "chat_group_id INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "TRUNCATE last_used_chat_group"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE last_used_chat_group " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN minecraft_profile_id INTEGER AFTER id"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: LastUsedChatGroup): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO last_used_chat_group(minecraft_profile_id, chat_group_id) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setInt(2, entity.chatGroup.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    minecraftProfileCache.put(entity.minecraftProfile.id, id)
                }
            }
        }
        return id
    }

    override fun update(entity: LastUsedChatGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE last_used_chat_group SET minecraft_profile_id = ?, chat_group_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setInt(2, entity.chatGroup.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                minecraftProfileCache.put(entity.minecraftProfile.id, entity.id)
            }
        }
    }

    override fun get(id: Int): LastUsedChatGroup? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var lastUsedChatGroup: LastUsedChatGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, minecraft_profile_id, chat_group_id FROM last_used_chat_group WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalLastUsedChatGroup = LastUsedChatGroup(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class).getChatGroup(resultSet.getInt("chat_group_id"))!!
                        )
                        lastUsedChatGroup = finalLastUsedChatGroup
                        cache.put(id, finalLastUsedChatGroup)
                        minecraftProfileCache.put(finalLastUsedChatGroup.minecraftProfile.id, id)
                    }
                }
            }
            return lastUsedChatGroup
        }
    }

    /**
     * Gets the last used chat group of a minecraftProfile.
     * If the minecraftProfile has never used a chat group, null is returned.
     *
     * @param minecraftProfile The minecraftProfile
     * @return The minecraftProfile's last used chat group, or null if no chat group has been used
     */
    fun get(minecraftProfile: RPKMinecraftProfile): LastUsedChatGroup? {
        if (minecraftProfileCache.containsKey(minecraftProfile.id)) {
            return get(minecraftProfileCache.get(minecraftProfile.id))
        } else {
            var lastUsedChatGroup: LastUsedChatGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, minecraft_profile_id, chat_group_id FROM last_used_chat_group WHERE minecraft_profile_id = ?"
                ).use { statement ->
                    statement.setInt(1, minecraftProfile.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalLastUsedChatGroup = LastUsedChatGroup(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class).getChatGroup(resultSet.getInt("chat_group_id"))!!
                        )
                        lastUsedChatGroup = finalLastUsedChatGroup
                        cache.put(finalLastUsedChatGroup.id, finalLastUsedChatGroup)
                        minecraftProfileCache.put(finalLastUsedChatGroup.minecraftProfile.id, finalLastUsedChatGroup.id)
                    }
                }
            }
            return lastUsedChatGroup
        }
    }

    override fun delete(entity: LastUsedChatGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM last_used_chat_group WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                minecraftProfileCache.remove(entity.minecraftProfile.id)
            }
        }
    }

}