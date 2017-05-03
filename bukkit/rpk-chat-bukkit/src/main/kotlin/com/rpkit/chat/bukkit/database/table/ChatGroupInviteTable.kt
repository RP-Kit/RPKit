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
import com.rpkit.chat.bukkit.chatgroup.ChatGroupInvite
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
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
import java.sql.Statement

/**
 * Represents chat group invite table.
 */
class ChatGroupInviteTable: Table<ChatGroupInvite> {

    private val plugin: RPKChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ChatGroupInvite>
    private val chatGroupCache: Cache<Int, MutableList<*>>
    private val minecraftProfileCache: Cache<Int, MutableList<*>>

    constructor(database: Database, plugin: RPKChatBukkit): super(database, ChatGroupInvite::class) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatGroupInvite::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        chatGroupCache = cacheManager.createCache("chatGroupCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        minecraftProfileCache = cacheManager.createCache("minecraftProfileCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chat_group_invite(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "chat_group_id INTEGER," +
                            "minecraft_profile_id INTEGER" +
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
                        "TRUNCATE chat_group_invite"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE chat_group_invite " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN minecraft_profile_id INTEGER"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: ChatGroupInvite): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO chat_group_invite(chat_group_id, minecraft_profile_id) VALUES(?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.chatGroup.id)
                statement.setInt(2, entity.minecraftProfile.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    val chatGroupInvites = chatGroupCache.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                    if (!chatGroupInvites.contains(entity.minecraftProfile.id)) {
                        chatGroupInvites.add(entity.id)
                    }
                    chatGroupCache.put(entity.chatGroup.id, chatGroupInvites)
                    val minecraftProfileInvites = minecraftProfileCache.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
                    if (!minecraftProfileInvites.contains(entity.minecraftProfile.id)) {
                        minecraftProfileInvites.add(entity.id)
                    }
                    minecraftProfileCache.put(entity.minecraftProfile.id, minecraftProfileInvites)
                }
            }
        }
        return id
    }

    override fun update(entity: ChatGroupInvite) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE chat_group_invite SET chat_group_id = ?, minecraft_profile_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.chatGroup.id)
                statement.setInt(2, entity.minecraftProfile.id)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                val chatGroupInvites = chatGroupCache.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                if (!chatGroupInvites.contains(entity.id)) {
                    chatGroupInvites.add(entity.id)
                }
                chatGroupCache.put(entity.chatGroup.id, chatGroupInvites)
                val minecraftProfileInvites = minecraftProfileCache.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
                if (!minecraftProfileInvites.contains(entity.minecraftProfile.id)) {
                    minecraftProfileInvites.add(entity.id)
                }
                minecraftProfileCache.put(entity.minecraftProfile.id, minecraftProfileInvites)
            }
        }
    }

    override fun get(id: Int): ChatGroupInvite? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatGroupInvite: ChatGroupInvite? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_group_id, minecraft_profile_id FROM chat_group_invite WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatGroupInvite = ChatGroupInvite(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class).getChatGroup(resultSet.getInt("chat_group_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))!!
                        )
                        chatGroupInvite = finalChatGroupInvite
                        cache.put(finalChatGroupInvite.id, finalChatGroupInvite)
                        val chatGroupInvites = chatGroupCache.get(finalChatGroupInvite.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                        if (!chatGroupInvites.contains(finalChatGroupInvite.id)) {
                            chatGroupInvites.add(finalChatGroupInvite.id)
                        }
                        chatGroupCache.put(finalChatGroupInvite.chatGroup.id, chatGroupInvites)
                        val minecraftProfileInvites = minecraftProfileCache.get(finalChatGroupInvite.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
                        if (!minecraftProfileInvites.contains(finalChatGroupInvite.minecraftProfile.id)) {
                            minecraftProfileInvites.add(finalChatGroupInvite.id)
                        }
                        minecraftProfileCache.put(finalChatGroupInvite.minecraftProfile.id, minecraftProfileInvites)
                    }
                }
            }
            return chatGroupInvite
        }
    }

    /**
     * Gets a list of invites for a particular chat group.
     *
     * @param chatGroup The chat group
     * @return A list of chat group invites
     */
    fun get(chatGroup: RPKChatGroup): List<ChatGroupInvite> {
        if (chatGroupCache.containsKey(chatGroup.id)) {
            return (chatGroupCache.get(chatGroup.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val chatGroupInvites = mutableListOf<ChatGroupInvite>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM chat_group_invite WHERE chat_group_id = ?"
                ).use { statement ->
                    statement.setInt(1, chatGroup.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val chatGroupInvite = get(resultSet.getInt("id"))
                        if (chatGroupInvite != null) {
                            chatGroupInvites.add(chatGroupInvite)
                        }
                    }
                    chatGroupCache.put(chatGroup.id, chatGroupInvites.map(ChatGroupInvite::id).toMutableList())
                }
            }
            return chatGroupInvites
        }
    }

    /**
     * Gets a list of chat group invites for a particular Minecraft profile.
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group invites for the Minecraft profile
     */
    fun get(minecraftProfile: RPKMinecraftProfile): List<ChatGroupInvite> {
        if (minecraftProfileCache.containsKey(minecraftProfile.id)) {
            return (minecraftProfileCache.get(minecraftProfile.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val chatGroupInvites = mutableListOf<ChatGroupInvite>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM chat_group_invite WHERE minecraft_profile_id = ?"
                ).use { statement ->
                    statement.setInt(1, minecraftProfile.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val chatGroupInvite = get(resultSet.getInt("id"))
                        if (chatGroupInvite != null) {
                            chatGroupInvites.add(chatGroupInvite)
                        }
                    }
                    minecraftProfileCache.put(minecraftProfile.id, chatGroupInvites.map { chatGroupInvite -> chatGroupInvite.id }.toMutableList())
                }
            }
            return chatGroupInvites
        }
    }

    override fun delete(entity: ChatGroupInvite) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_group_invite WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                val chatGroupMembers = chatGroupCache.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                chatGroupMembers.remove(entity.id)
                chatGroupCache.put(entity.chatGroup.id, chatGroupMembers)
                val minecraftProfileMembers = minecraftProfileCache.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
                minecraftProfileMembers.remove(entity.id)
                minecraftProfileCache.put(entity.minecraftProfile.id, minecraftProfileMembers)
            }
        }
    }

}