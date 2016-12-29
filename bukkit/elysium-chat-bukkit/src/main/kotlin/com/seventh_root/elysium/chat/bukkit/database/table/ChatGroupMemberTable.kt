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
import com.seventh_root.elysium.chat.bukkit.chatgroup.ChatGroupMember
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroup
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroupProvider
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
import java.sql.Statement

/**
 * Represents the chat group member table.
 */
class ChatGroupMemberTable: Table<ChatGroupMember> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ChatGroupMember>
    private val chatGroupCache: Cache<Int, MutableList<*>>
    private val playerCache: Cache<Int, MutableList<*>>

    constructor(database: Database, plugin: ElysiumChatBukkit): super(database, ChatGroupMember::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatGroupMember::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        chatGroupCache = cacheManager.createCache("chatGroupCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        playerCache = cacheManager.createCache("playerCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chat_group_member(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "chat_group_id INTEGER," +
                            "player_id INTEGER" +
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

    override fun insert(entity: ChatGroupMember): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO chat_group_member(chat_group_id, player_id) VALUES(?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.chatGroup.id)
                statement.setInt(2, entity.player.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    val chatGroupMembers = chatGroupCache.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                    if (!chatGroupMembers.contains(entity.id)) {
                        chatGroupMembers.add(entity.id)
                    }
                    chatGroupCache.put(entity.chatGroup.id, chatGroupMembers)
                    val playerMembers = playerCache.get(entity.player.id) as? MutableList<Int> ?: mutableListOf<Int>()
                    if (!playerMembers.contains(entity.id)) {
                        playerMembers.add(entity.id)
                    }
                    playerCache.put(entity.player.id, playerMembers)
                }
            }
        }
        return id
    }

    override fun update(entity: ChatGroupMember) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE chat_group_member SET chat_group_id = ?, player_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.chatGroup.id)
                statement.setInt(2, entity.player.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                val chatGroupMembers = chatGroupCache.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                if (!chatGroupMembers.contains(entity.id)) {
                    chatGroupMembers.add(entity.id)
                }
                chatGroupCache.put(entity.chatGroup.id, chatGroupMembers)
                val playerMembers = playerCache.get(entity.player.id) as? MutableList<Int> ?: mutableListOf<Int>()
                if (!playerMembers.contains(entity.id)) {
                    playerMembers.add(entity.id)
                }
                playerCache.put(entity.player.id, playerMembers)
            }
        }
    }

    override fun get(id: Int): ChatGroupMember? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatGroupMember: ChatGroupMember? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_group_id, player_id FROM chat_group_member WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatGroupMember = ChatGroupMember(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(ElysiumChatGroupProvider::class).getChatGroup(resultSet.getInt("chat_group_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatGroupMember = finalChatGroupMember
                        cache.put(finalChatGroupMember.id, finalChatGroupMember)
                        val chatGroupMembers = chatGroupCache.get(finalChatGroupMember.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                        if (!chatGroupMembers.contains(finalChatGroupMember.id)) {
                            chatGroupMembers.add(finalChatGroupMember.id)
                        }
                        chatGroupCache.put(finalChatGroupMember.chatGroup.id, chatGroupMembers)
                        val playerMembers = playerCache.get(finalChatGroupMember.player.id) as? MutableList<Int> ?: mutableListOf<Int>()
                        if (!playerMembers.contains(finalChatGroupMember.id)) {
                            playerMembers.add(finalChatGroupMember.id)
                        }
                        playerCache.put(finalChatGroupMember.player.id, playerMembers)
                    }
                }
            }
            return chatGroupMember
        }
    }

    /**
     * Gets a list of members of a specific chat group.
     *
     * @param chatGroup The chat group
     * @return A list of members of the chat group
     */
    fun get(chatGroup: ElysiumChatGroup): List<ChatGroupMember> {
        if (chatGroupCache.containsKey(chatGroup.id)) {
            return (chatGroupCache.get(chatGroup.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val chatGroupMembers = mutableListOf<ChatGroupMember>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM chat_group_member WHERE chat_group_id = ?"
                ).use { statement ->
                    statement.setInt(1, chatGroup.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val chatGroupMember = get(resultSet.getInt("id"))
                        if (chatGroupMember != null) {
                            chatGroupMembers.add(chatGroupMember)
                        }
                    }
                    chatGroupCache.put(chatGroup.id, chatGroupMembers.map { chatGroupMember -> chatGroupMember.id }.toMutableList())
                }
            }
            return chatGroupMembers
        }
    }

    /**
     * Gets a list of chat group member instances for a player
     *
     * @param player The player
     * @return A list of chat group member instances
     */
    fun get(player: ElysiumPlayer): List<ChatGroupMember> {
        if (playerCache.containsKey(player.id)) {
            return (playerCache.get(player.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val chatGroupMembers = mutableListOf<ChatGroupMember>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM chat_group_member WHERE player_id = ?"
                ).use { statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val chatGroupMember = get(resultSet.getInt("id"))
                        if (chatGroupMember != null) {
                            chatGroupMembers.add(chatGroupMember)
                        }
                    }
                    playerCache.put(player.id, chatGroupMembers.map { chatGroupMember -> chatGroupMember.id }.toMutableList())
                }
            }
            return chatGroupMembers
        }
    }

    override fun delete(entity: ChatGroupMember) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_group_member WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                val chatGroupMembers = chatGroupCache.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                chatGroupMembers.remove(entity.id)
                chatGroupCache.put(entity.chatGroup.id, chatGroupMembers)
                val playerMembers = playerCache.get(entity.player.id) as? MutableList<Int> ?: mutableListOf<Int>()
                playerMembers.remove(entity.id)
                playerCache.put(entity.player.id, playerMembers)
            }
        }
    }

}