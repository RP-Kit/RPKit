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
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelImpl
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.awt.Color
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*

class ElysiumChatChannelTable: Table<ElysiumChatChannel> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumChatChannel>
    private val nameCache: Cache<String, Int>

    constructor(database: Database, plugin: ElysiumChatBukkit): super(database, ElysiumChatChannel::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumChatChannel::class.java,
                        ResourcePoolsBuilder.heap(20L)).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(20L)).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS elysium_chat_channel(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)," +
                            "color_red INTEGER," +
                            "color_green INTEGER," +
                            "color_blue INTEGER," +
                            "format_string VARCHAR(256)," +
                            "match_pattern VARCHAR(256)," +
                            "radius INTEGER," +
                            "clear_radius INTEGER," +
                            "irc_enabled BOOLEAN," +
                            "irc_channel VARCHAR(256)," +
                            "irc_whitelist BOOLEAN," +
                            "joined_by_default BOOLEAN" +
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
            database.setTableVersion(this, "0.3.0")
        }
        if (database.getTableVersion(this) == "0.1.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "ALTER TABLE chat_channel_listener ADD COLUMN id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST"
                ).use { statement ->
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                        "ALTER TABLE chat_channel_speaker ADD COLUMN id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST"
                ).use { statement ->
                    statement.executeUpdate()
                }
            }
            database.setTableVersion(this, "0.3.0")
        }
    }

    override fun insert(entity: ElysiumChatChannel): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_chat_channel(name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, entity.name)
                statement.setInt(2, entity.color.red)
                statement.setInt(3, entity.color.green)
                statement.setInt(4, entity.color.blue)
                statement.setString(5, entity.formatString)
                statement.setInt(6, entity.radius)
                statement.setInt(7, entity.clearRadius)
                statement.setString(8, entity.matchPattern)
                statement.setBoolean(9, entity.isIRCEnabled)
                statement.setString(10, entity.ircChannel)
                statement.setBoolean(11, entity.isIRCWhitelist)
                statement.setBoolean(12, entity.isJoinedByDefault)
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
    }

    override fun update(entity: ElysiumChatChannel) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_chat_channel SET name = ?, color_red = ?, color_green = ?, color_blue = ?, format_string = ?, radius = ?, clear_radius = ?, match_pattern = ?, irc_enabled = ?, irc_channel = ?, irc_whitelist = ?, joined_by_default = ? WHERE id = ?").use { statement ->
                statement.setString(1, entity.name)
                statement.setInt(2, entity.color.red)
                statement.setInt(3, entity.color.green)
                statement.setInt(4, entity.color.blue)
                statement.setString(5, entity.formatString)
                statement.setInt(6, entity.radius)
                statement.setInt(7, entity.clearRadius)
                statement.setString(8, entity.matchPattern)
                statement.setBoolean(9, entity.isIRCEnabled)
                statement.setString(10, entity.ircChannel)
                statement.setBoolean(11, entity.isIRCWhitelist)
                statement.setBoolean(12, entity.isJoinedByDefault)
                statement.setInt(13, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                nameCache.put(entity.name, entity.id)
            }
        }
    }

    override fun get(id: Int): ElysiumChatChannel? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatChannel: ElysiumChatChannel? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM elysium_chat_channel WHERE id = ?").use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatChannel = ElysiumChatChannelImpl(
                                plugin = plugin,
                                id = resultSet.getInt("id"),
                                name = resultSet.getString("name"),
                                color = Color(
                                        resultSet.getInt("color_red"),
                                        resultSet.getInt("color_green"),
                                        resultSet.getInt("color_blue")
                                ),
                                formatString = resultSet.getString("format_string"),
                                radius = resultSet.getInt("radius"),
                                clearRadius = resultSet.getInt("clear_radius"),
                                matchPattern = resultSet.getString("match_pattern"),
                                isIRCEnabled = resultSet.getBoolean("irc_enabled"),
                                ircChannel = resultSet.getString("irc_channel"),
                                isIRCWhitelist = resultSet.getBoolean("irc_whitelist"),
                                isJoinedByDefault = resultSet.getBoolean("joined_by_default")
                        )
                        chatChannel = finalChatChannel
                        cache.put(id, finalChatChannel)
                        nameCache.put(finalChatChannel.name, id)
                    }
                }
            }
            return chatChannel
        }
    }

    fun get(name: String): ElysiumChatChannel? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            var chatChannel: ElysiumChatChannel? = null
            plugin.core.database.createConnection().use { connection ->
                connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM elysium_chat_channel WHERE name = ?").use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val finalChatChannel = ElysiumChatChannelImpl(
                                plugin = plugin,
                                id = id,
                                name = resultSet.getString("name"),
                                color = Color(
                                        resultSet.getInt("color_red"),
                                        resultSet.getInt("color_green"),
                                        resultSet.getInt("color_blue")
                                ),
                                formatString = resultSet.getString("format_string"),
                                radius = resultSet.getInt("radius"),
                                clearRadius = resultSet.getInt("clear_radius"),
                                matchPattern = resultSet.getString("match_pattern"),
                                isIRCEnabled = resultSet.getBoolean("irc_enabled"),
                                ircChannel = resultSet.getString("irc_channel"),
                                isIRCWhitelist = resultSet.getBoolean("irc_whitelist"),
                                isJoinedByDefault = resultSet.getBoolean("joined_by_default")
                        )
                        chatChannel = finalChatChannel
                        cache.put(id, finalChatChannel)
                        nameCache.put(finalChatChannel.name, id)
                    }
                }
            }
            return chatChannel
        }
    }

    fun getAll(): Collection<ElysiumChatChannel> {
        val chatChannels = ArrayList<ElysiumChatChannel>()
        database.createConnection().use { connection ->
            connection.prepareStatement("SELECT id FROM elysium_chat_channel").use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val chatChannel = get(resultSet.getInt("id"))
                    if (chatChannel != null) {
                        cache.put(chatChannel.id, chatChannel)
                        nameCache.put(chatChannel.name, chatChannel.id)
                        chatChannels.add(chatChannel)
                    }
                }
            }
        }
        return chatChannels
    }

    override fun delete(entity: ElysiumChatChannel) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?").use { speakersStatement ->
                speakersStatement.setInt(1, entity.id)
                speakersStatement.executeUpdate()
                cache.remove(entity.id)
                nameCache.remove(entity.name)
            }
        }
        val chatChannelListenerTable = database.getTable(ChatChannelListenerTable::class)
        val chatChannelListeners = chatChannelListenerTable.get(entity)
        chatChannelListeners.forEach { chatChannelListenerTable.delete(it) }
        val chatChannelSpeakerTable = database.getTable(ChatChannelSpeakerTable::class)
        val chatChannelSpeakers = chatChannelSpeakerTable.get(entity)
        chatChannelSpeakers.forEach { chatChannelSpeakerTable.delete(it) }
    }
}
