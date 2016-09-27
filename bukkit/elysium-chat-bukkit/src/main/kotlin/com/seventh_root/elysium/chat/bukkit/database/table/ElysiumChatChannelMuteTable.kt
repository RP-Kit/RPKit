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
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.mute.ElysiumChatChannelMute
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS

class ElysiumChatChannelMuteTable: Table<ElysiumChatChannelMute> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumChatChannelMute>

    constructor(database: Database, plugin: ElysiumChatBukkit): super(database, ElysiumChatChannelMute::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumChatChannelMute::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_chat_channel_mute(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "chat_channel_id INTEGER" +
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

    override fun insert(entity: ElysiumChatChannelMute): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_chat_channel_mute(player_id, chat_channel_id) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.chatChannel.id)
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

    override fun update(entity: ElysiumChatChannelMute) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_chat_channel_mute SET player_id = ?, chat_channel_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.chatChannel.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): ElysiumChatChannelMute? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatChannelMute: ElysiumChatChannelMute? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, player_id, chat_channel_id FROM elysium_chat_channel_mute WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                        chatChannelMute = ElysiumChatChannelMute(
                                resultSet.getInt("id"),
                                playerProvider.getPlayer(resultSet.getInt("player_id"))!!,
                                chatChannelProvider.getChatChannel(resultSet.getInt("chat_channel_id"))!!
                        )
                        cache.put(id, chatChannelMute)
                    }
                }
            }
            return chatChannelMute
        }
    }

    fun get(player: ElysiumPlayer, chatChannel: ElysiumChatChannel): ElysiumChatChannelMute? {
        var chatChannelMute: ElysiumChatChannelMute? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, player_id, chat_channel_id FROM elysium_chat_channel_mute WHERE player_id = ? AND chat_channel_id = ?"
            ).use { statement ->
                statement.setInt(1, player.id)
                statement.setInt(2, chatChannel.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                    val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class)
                    val finalChatChannelMute = ElysiumChatChannelMute(resultSet.getInt("id"),
                            playerProvider.getPlayer(resultSet.getInt("player_id"))!!,
                            chatChannelProvider.getChatChannel(resultSet.getInt("chat_channel_id"))!!
                    )
                    chatChannelMute = finalChatChannelMute
                    cache.put(finalChatChannelMute.id, finalChatChannelMute)
                }
            }
        }
        return chatChannelMute
    }

    override fun delete(entity: ElysiumChatChannelMute) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_chat_channel_mute WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }


}
