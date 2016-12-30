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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
import com.rpkit.chat.bukkit.chatgroup.LastUsedChatGroup
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents the last used chat group table
 */
class LastUsedChatGroupTable: Table<LastUsedChatGroup> {

    private val plugin: RPKChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, LastUsedChatGroup>
    private val playerCache: Cache<Int, Int>

    constructor(database: Database, plugin: RPKChatBukkit): super(database, LastUsedChatGroup::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, LastUsedChatGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
        playerCache = cacheManager.createCache("playerCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS last_used_chat_group(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "chat_group_id INTEGER" +
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

    override fun insert(entity: LastUsedChatGroup): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO last_used_chat_group(player_id, chat_group_id) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.chatGroup.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    playerCache.put(entity.player.id, id)
                }
            }
        }
        return id
    }

    override fun update(entity: LastUsedChatGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE last_used_chat_group SET player_id = ?, chat_group_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.chatGroup.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                playerCache.put(entity.player.id, entity.id)
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
                        "SELECT id, player_id, chat_group_id FROM last_used_chat_group WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalLastUsedChatGroup = LastUsedChatGroup(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class).getChatGroup(resultSet.getInt("chat_group_id"))!!
                        )
                        lastUsedChatGroup = finalLastUsedChatGroup
                        cache.put(id, finalLastUsedChatGroup)
                        playerCache.put(finalLastUsedChatGroup.player.id, id)
                    }
                }
            }
            return lastUsedChatGroup
        }
    }

    /**
     * Gets the last used chat group of a player.
     * If the player has never used a chat group, null is returned.
     *
     * @param player The player
     * @return The player's last used chat group, or null if no chat group has been used
     */
    fun get(player: RPKPlayer): LastUsedChatGroup? {
        if (playerCache.containsKey(player.id)) {
            return get(playerCache.get(player.id))
        } else {
            var lastUsedChatGroup: LastUsedChatGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, player_id, chat_group_id FROM last_used_chat_group WHERE player_id = ?"
                ).use { statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalLastUsedChatGroup = LastUsedChatGroup(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class).getChatGroup(resultSet.getInt("chat_group_id"))!!
                        )
                        lastUsedChatGroup = finalLastUsedChatGroup
                        cache.put(finalLastUsedChatGroup.id, finalLastUsedChatGroup)
                        playerCache.put(finalLastUsedChatGroup.player.id, finalLastUsedChatGroup.id)
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
                playerCache.remove(entity.player.id)
            }
        }
    }

}