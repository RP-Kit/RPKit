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
import com.seventh_root.elysium.chat.bukkit.chatchannel.ChatChannelSpeaker
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
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


class ChatChannelSpeakerTable: Table<ChatChannelSpeaker> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ChatChannelSpeaker>
    private val chatChannelCache: Cache<Int, MutableList<*>>
    private val playerCache: Cache<Int, Int>

    constructor(database: Database, plugin: ElysiumChatBukkit): super(database, ChatChannelSpeaker::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatChannelSpeaker::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        chatChannelCache = cacheManager.createCache("chatChannelCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.javaObjectType,
                        ResourcePoolsBuilder.heap(20L)).build())
        playerCache = cacheManager.createCache("playerCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chat_channel_speaker(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "chat_channel_id INTEGER" +
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

    override fun insert(entity: ChatChannelSpeaker): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO chat_channel_speaker(player_id, chat_channel_id) VALUES(?, ?)",
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
                    val chatChannelSpeakers = chatChannelCache.get(entity.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                    if (!chatChannelSpeakers.contains(id)) {
                        chatChannelSpeakers.add(id)
                    }
                    chatChannelCache.put(entity.chatChannel.id, chatChannelSpeakers)
                    playerCache.put(entity.player.id, entity.id)
                }
            }
        }
        return id
    }

    override fun update(entity: ChatChannelSpeaker) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE chat_channel_speaker SET chat_channel_id = ?, player_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.chatChannel.id)
                statement.setInt(2, entity.player.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                val chatChannelSpeakers = chatChannelCache.get(entity.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                if (!chatChannelSpeakers.contains(entity.id)) {
                    chatChannelSpeakers.add(entity.id)
                }
                chatChannelCache.put(entity.chatChannel.id, chatChannelSpeakers)
                playerCache.put(entity.player.id, entity.id)
            }
        }
    }

    override fun get(id: Int): ChatChannelSpeaker? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatChannelSpeaker: ChatChannelSpeaker? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_speaker WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatChannelSpeaker = ChatChannelSpeaker(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(resultSet.getInt("chat_channel_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatChannelSpeaker = finalChatChannelSpeaker
                        cache.put(id, finalChatChannelSpeaker)
                        val chatChannelSpeakers = chatChannelCache.get(finalChatChannelSpeaker.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!chatChannelSpeakers.contains(finalChatChannelSpeaker.id)) {
                            chatChannelSpeakers.add(finalChatChannelSpeaker.id)
                        }
                        chatChannelCache.put(finalChatChannelSpeaker.chatChannel.id, chatChannelSpeakers)
                        playerCache.put(finalChatChannelSpeaker.player.id, finalChatChannelSpeaker.id)
                    }
                }
            }
            return chatChannelSpeaker
        }
    }

    fun get(player: ElysiumPlayer): ChatChannelSpeaker? {
        if (playerCache.containsKey(player.id)) {
            return get(playerCache.get(player.id))
        } else {
            var chatChannelSpeaker: ChatChannelSpeaker? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_speaker WHERE player_id = ?"
                ).use { statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val finalChatChannelSpeaker = ChatChannelSpeaker(
                                id,
                                plugin.core.serviceManager.getServiceProvider(ElysiumChatChannelProvider::class).getChatChannel(resultSet.getInt("chat_channel_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatChannelSpeaker = finalChatChannelSpeaker
                        cache.put(id, finalChatChannelSpeaker)
                        val chatChannelSpeakers = chatChannelCache.get(finalChatChannelSpeaker.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!chatChannelSpeakers.contains(finalChatChannelSpeaker.id)) {
                            chatChannelSpeakers.add(finalChatChannelSpeaker.id)
                        }
                        chatChannelCache.put(finalChatChannelSpeaker.chatChannel.id, chatChannelSpeakers)
                        playerCache.put(finalChatChannelSpeaker.player.id, finalChatChannelSpeaker.id)
                    }
                }
            }
            return chatChannelSpeaker
        }
    }

    fun get(chatChannel: ElysiumChatChannel): List<ChatChannelSpeaker> {
        if (chatChannelCache.containsKey(chatChannel.id)) {
            return (chatChannelCache.get(chatChannel.id) as MutableList<Int>).map { speakerId -> get(speakerId)!! }
        } else {
            val chatChannelSpeakers = mutableListOf<ChatChannelSpeaker>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_speaker WHERE chat_channel_id = ?"
                ).use { statement ->
                    statement.setInt(1, chatChannel.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        chatChannelSpeakers.add(get(resultSet.getInt("id"))!!)
                    }
                }
            }
            return chatChannelSpeakers
        }
    }

    override fun delete(entity: ChatChannelSpeaker) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_channel_speaker WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                chatChannelCache.remove(entity.chatChannel.id)
                playerCache.remove(entity.player.id)
            }
        }
    }
}