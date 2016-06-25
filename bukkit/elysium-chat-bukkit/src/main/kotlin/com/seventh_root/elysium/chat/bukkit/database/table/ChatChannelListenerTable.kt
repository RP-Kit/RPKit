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
import com.seventh_root.elysium.chat.bukkit.chatchannel.ChatChannelListener
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS


class ChatChannelListenerTable: Table<ChatChannelListener> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ChatChannelListener>
    private val chatChannelCache: Cache<Int, MutableList<*>>
    private val playerCache: Cache<Int, MutableList<*>>

    constructor(plugin: ElysiumChatBukkit, database: Database): super(database, ChatChannelListener::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatChannelListener::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        chatChannelCache = cacheManager.createCache("chatChannelCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.javaObjectType,
                        ResourcePoolsBuilder.heap(20L)).build())
        playerCache = cacheManager.createCache("playerCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chat_channel_listener(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "chat_channel_id INTEGER," +
                            "FOREIGN KEY(player_id) REFERENCES elysium_player(id)," +
                            "FOREIGN KEY(chat_channel_id) REFERENCES elysium_chat_channel(id)" +
                            ")").use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.3.0")
        }
    }

    override fun insert(`object`: ChatChannelListener): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO chat_channel_listener(player_id, chat_channel_id) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, `object`.player.id)
                statement.setInt(2, `object`.chatChannel.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    val chatChannelListeners = chatChannelCache.get(`object`.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                    if (!chatChannelListeners.contains(id)) {
                        chatChannelListeners.add(id)
                    }
                    chatChannelCache.put(`object`.chatChannel.id, chatChannelListeners)
                    val playerChannels = playerCache.get(`object`.player.id) as? MutableList<Int>?:mutableListOf<Int>()
                    if (!playerChannels.contains(`object`.id)) {
                        playerChannels.add(`object`.id)
                    }
                    playerCache.put(`object`.player.id, playerChannels)
                }
            }
        }
        return id
    }

    override fun update(`object`: ChatChannelListener) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE chat_channel_listener SET chat_channel_id = ?, player_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.chatChannel.id)
                statement.setInt(2, `object`.player.id)
                statement.setInt(3, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                val chatChannelListeners = chatChannelCache.get(`object`.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                if (!chatChannelListeners.contains(`object`.id)) {
                    chatChannelListeners.add(`object`.id)
                }
                chatChannelCache.put(`object`.chatChannel.id, chatChannelListeners)
                val playerChannels = playerCache.get(`object`.player.id) as? MutableList<Int>?:mutableListOf<Int>()
                if (!playerChannels.contains(`object`.id)) {
                    playerChannels.add(`object`.id)
                }
                playerCache.put(`object`.player.id, playerChannels)
            }
        }
    }

    override fun get(id: Int): ChatChannelListener? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatChannelListener: ChatChannelListener? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_listener WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatChannelListener = ChatChannelListener(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(resultSet.getInt("chat_channel_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatChannelListener = finalChatChannelListener
                        cache.put(id, finalChatChannelListener)
                        val chatChannelListeners = chatChannelCache.get(finalChatChannelListener.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!chatChannelListeners.contains(finalChatChannelListener.id)) {
                            chatChannelListeners.add(finalChatChannelListener.id)
                        }
                        chatChannelCache.put(finalChatChannelListener.chatChannel.id, chatChannelListeners)
                        val playerChannels = playerCache.get(finalChatChannelListener.player.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!playerChannels.contains(finalChatChannelListener.id)) {
                            playerChannels.add(finalChatChannelListener.id)
                        }
                        playerCache.put(finalChatChannelListener.player.id, playerChannels)
                    }
                }
            }
            return chatChannelListener
        }
    }

    fun get(player: ElysiumPlayer): List<ChatChannelListener> {
        if (playerCache.containsKey(player.id)) {
            return (playerCache.get(player.id) as? MutableList<Int>)?.map { listenerId -> get(listenerId)!! }?:mutableListOf<ChatChannelListener>()
        } else {
            val chatChannelListeners = mutableListOf<ChatChannelListener>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_listener WHERE player_id = ?"
                ).use { statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val finalChatChannelListener = ChatChannelListener(
                                id,
                                plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(resultSet.getInt("chat_channel_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatChannelListeners.add(finalChatChannelListener)
                        cache.put(id, finalChatChannelListener)
                        val chatChannelListeners = chatChannelCache.get(finalChatChannelListener.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!chatChannelListeners.contains(finalChatChannelListener.id)) {
                            chatChannelListeners.add(finalChatChannelListener.id)
                        }
                        chatChannelCache.put(finalChatChannelListener.chatChannel.id, chatChannelListeners)
                        val playerChannels = playerCache.get(finalChatChannelListener.player.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!playerChannels.contains(finalChatChannelListener.id)) {
                            playerChannels.add(finalChatChannelListener.id)
                        }
                        playerCache.put(finalChatChannelListener.player.id, playerChannels)
                    }
                }
            }
            return chatChannelListeners
        }
    }

    fun get(chatChannel: ElysiumChatChannel): List<ChatChannelListener> {
        if (chatChannelCache.containsKey(chatChannel.id)) {
            return (chatChannelCache.get(chatChannel.id) as MutableList<Int>).map { listenerId -> get(listenerId)!! }
        } else {
            val chatChannelListeners = mutableListOf<ChatChannelListener>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_listener WHERE chat_channel_id = ?"
                ).use { statement ->
                    statement.setInt(1, chatChannel.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        chatChannelListeners.add(get(resultSet.getInt("id"))!!)
                    }
                }
            }
            return chatChannelListeners
        }
    }

    override fun delete(`object`: ChatChannelListener) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_channel_listener WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.id)
                statement.executeUpdate()
                cache.remove(`object`.id)
                chatChannelCache.remove(`object`.chatChannel.id)
                playerCache.remove(`object`.player.id)
            }
        }
    }
}