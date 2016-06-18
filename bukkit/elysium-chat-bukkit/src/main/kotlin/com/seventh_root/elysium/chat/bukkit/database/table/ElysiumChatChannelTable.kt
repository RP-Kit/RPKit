package com.seventh_root.elysium.chat.bukkit.database.table

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.ChatChannelListener
import com.seventh_root.elysium.chat.bukkit.chatchannel.ChatChannelSpeaker
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
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

    constructor(plugin: ElysiumChatBukkit, database: Database): super(database, ElysiumChatChannel::class.java) {
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

    override fun insert(`object`: ElysiumChatChannel): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_chat_channel(name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, `object`.name)
                statement.setInt(2, `object`.color.red)
                statement.setInt(3, `object`.color.green)
                statement.setInt(4, `object`.color.blue)
                statement.setString(5, `object`.formatString)
                statement.setInt(6, `object`.radius)
                statement.setInt(7, `object`.clearRadius)
                statement.setString(8, `object`.matchPattern)
                statement.setBoolean(9, `object`.isIRCEnabled)
                statement.setString(10, `object`.ircChannel)
                statement.setBoolean(11, `object`.isIRCWhitelist)
                statement.setBoolean(12, `object`.isJoinedByDefault)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    nameCache.put(`object`.name, id)
                }
            }
        }
        return id
    }

    override fun update(`object`: ElysiumChatChannel) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_chat_channel SET name = ?, color_red = ?, color_green = ?, color_blue = ?, format_string = ?, radius = ?, clear_radius = ?, match_pattern = ?, irc_enabled = ?, irc_channel = ?, irc_whitelist = ?, joined_by_default = ? WHERE id = ?").use { statement ->
                statement.setString(1, `object`.name)
                statement.setInt(2, `object`.color.red)
                statement.setInt(3, `object`.color.green)
                statement.setInt(4, `object`.color.blue)
                statement.setString(5, `object`.formatString)
                statement.setInt(6, `object`.radius)
                statement.setInt(7, `object`.clearRadius)
                statement.setString(8, `object`.matchPattern)
                statement.setBoolean(9, `object`.isIRCEnabled)
                statement.setString(10, `object`.ircChannel)
                statement.setBoolean(11, `object`.isIRCWhitelist)
                statement.setBoolean(12, `object`.isJoinedByDefault)
                statement.setInt(13, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                nameCache.put(`object`.name, `object`.id)
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
                        val finalChatChannel = ElysiumChatChannel(
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
                        (database.getTable(ChatChannelSpeaker::class.java) as? ChatChannelSpeakerTable)?.get(finalChatChannel)?.map { speaker -> speaker.player }?.forEach { speaker -> finalChatChannel.addSpeaker0(speaker) }
                        (database.getTable(ChatChannelListener::class.java) as? ChatChannelListenerTable)?.get(finalChatChannel)?.map { listener -> listener.player }?.forEach { listener -> finalChatChannel.addListener0(listener) }
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
                        val finalChatChannel = ElysiumChatChannel(
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
                        (database.getTable(ChatChannelSpeaker::class.java) as? ChatChannelSpeakerTable)?.get(finalChatChannel)?.map { speaker -> speaker.player }?.forEach { speaker -> finalChatChannel.addSpeaker0(speaker) }
                        (database.getTable(ChatChannelListener::class.java) as? ChatChannelListenerTable)?.get(finalChatChannel)?.map { listener -> listener.player }?.forEach { listener -> finalChatChannel.addListener0(listener) }
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
        plugin.core.database.createConnection().use { connection ->
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

    override fun delete(`object`: ElysiumChatChannel) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?").use { speakersStatement ->
                speakersStatement.setInt(1, `object`.id)
                speakersStatement.executeUpdate()
                cache.remove(`object`.id)
                nameCache.remove(`object`.name)
            }
        }
        val chatChannelListenerTable = database.getTable(ChatChannelListener::class.java) as? ChatChannelListenerTable
        val chatChannelListeners = chatChannelListenerTable?.get(`object`)
        chatChannelListeners?.forEach { chatChannelListenerTable?.delete(it) }
        val chatChannelSpeakerTable = database.getTable(ChatChannelSpeaker::class.java) as? ChatChannelSpeakerTable
        val chatChannelSpeakers = chatChannelSpeakerTable?.get(`object`)
        chatChannelSpeakers?.forEach { chatChannelSpeakerTable?.delete(it) }
    }
}
