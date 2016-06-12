package com.seventh_root.elysium.chat.bukkit.database.table

import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import java.awt.Color
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*

class BukkitChatChannelTable: Table<BukkitChatChannel> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, BukkitChatChannel>
    private val nameCache: Cache<String, Int>
    private val playerCache: Cache<Int, Int>

    constructor(plugin: ElysiumChatBukkit, database: Database): super(database, BukkitChatChannel::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, BukkitChatChannel::class.java).build())
        nameCache = cacheManager.createCache("nameCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType).build())
        playerCache = cacheManager.createCache("playerCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_chat_channel(" +
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
                        ")").use({ statement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS chat_channel_listener(" +
                                "chat_channel_id INTEGER," +
                                "player_id INTEGER," +
                                "FOREIGN KEY(chat_channel_id) REFERENCES bukkit_chat_channel(id)," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)" +
                        ")").use({ statement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS chat_channel_speaker(" +
                                "chat_channel_id INTEGER," +
                                "player_id INTEGER," +
                                "FOREIGN KEY(chat_channel_id) REFERENCES bukkit_chat_channel(id)," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)" +
                        ")").use({ statement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(`object`: BukkitChatChannel): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_chat_channel(name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
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
                        insertListeners(`object`)
                        insertSpeakers(`object`)
                        cache.put(id, `object`)
                        nameCache.put(`object`.name, id)
                        `object`.speakers.forEach { speaker -> playerCache.put(speaker.id, id) }
                    }
                })
            }
            return id
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return 0
    }

    private fun insertListeners(chatChannel: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO chat_channel_listener(chat_channel_id, player_id) VALUES(?, ?)").use({ statement ->
                    for (listener in chatChannel.listeners) {
                        statement.setInt(1, chatChannel.id)
                        statement.setInt(2, listener.id)
                        statement.executeUpdate()
                    }
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    private fun insertSpeakers(chatChannel: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO chat_channel_speaker(chat_channel_id, player_id) VALUES(?, ?)").use({ statement ->
                    for (speaker in chatChannel.speakers) {
                        statement.setInt(1, chatChannel.id)
                        statement.setInt(2, speaker.id)
                        statement.executeUpdate()
                        playerCache.put(speaker.id, chatChannel.id)
                    }
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    override fun update(`object`: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_chat_channel SET name = ?, color_red = ?, color_green = ?, color_blue = ?, format_string = ?, radius = ?, clear_radius = ?, match_pattern = ?, irc_enabled = ?, irc_channel = ?, irc_whitelist = ?, joined_by_default = ? WHERE id = ?").use({ statement ->
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
                    deleteListeners(`object`)
                    deleteSpeakers(`object`)
                    insertListeners(`object`)
                    insertSpeakers(`object`)
                    cache.put(`object`.id, `object`)
                    nameCache.put(`object`.name, `object`.id)
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        deleteListeners(`object`)
        deleteSpeakers(`object`)
        insertListeners(`object`)
        insertSpeakers(`object`)
    }

    private fun deleteListeners(chatChannel: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM chat_channel_listener WHERE chat_channel_id = ?").use({ statement ->
                    statement.setInt(1, chatChannel.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    private fun deleteSpeakers(chatChannel: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ statement ->
                    statement.setInt(1, chatChannel.id)
                    statement.executeUpdate()
                })
                val cachedSpeakers = playerCache
                        .filter { cacheEntry -> cacheEntry.value as Int == chatChannel.id }
                        .map { cacheEntry -> cacheEntry.key }
                cachedSpeakers.forEach { playerId -> playerCache.remove(playerId) }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    override fun get(id: Int): BukkitChatChannel? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            try {
                var chatChannel: BukkitChatChannel? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel WHERE id = ?").use({ statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            chatChannel = BukkitChatChannel(
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
                            if (chatChannel != null) {
                                val finalChatChannel = chatChannel!!
                                connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                                    listenerStatement.setInt(1, finalChatChannel.id)
                                    val listenerResultSet = listenerStatement.executeQuery()
                                    while (listenerResultSet.next()) {
                                        finalChatChannel.addListener(database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                                    }
                                })
                                connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                                    speakerStatement.setInt(1, finalChatChannel.id)
                                    val speakerResultSet = speakerStatement.executeQuery()
                                    while (speakerResultSet.next()) {
                                        finalChatChannel.addSpeaker(database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                                    }
                                })
                                cache.put(id, finalChatChannel)
                                nameCache.put(finalChatChannel.name, id)
                                finalChatChannel.speakers.forEach { speaker -> playerCache.put(speaker.id, id) }
                            }
                        }
                    })
                }
                return chatChannel
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    fun get(name: String): BukkitChatChannel? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            var chatChannel: BukkitChatChannel? = null
            try {
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel WHERE name = ?").use({ statement ->
                        statement.setString(1, name)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            chatChannel = BukkitChatChannel(
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
                            if (chatChannel != null) {
                                val finalChatChannel = chatChannel!!
                                connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                                    listenerStatement.setInt(1, finalChatChannel.id)
                                    val listenerResultSet = listenerStatement.executeQuery()
                                    while (listenerResultSet.next()) {
                                        finalChatChannel.addListener(plugin.core.database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                                    }
                                })
                                connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                                    speakerStatement.setInt(1, finalChatChannel.id)
                                    val speakerResultSet = speakerStatement.executeQuery()
                                    while (speakerResultSet.next()) {
                                        finalChatChannel.addSpeaker(plugin.core.database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                                    }
                                })
                                cache.put(finalChatChannel.id, chatChannel)
                                nameCache.put(finalChatChannel.name, finalChatChannel.id)
                                finalChatChannel.speakers.forEach { speaker -> playerCache.put(speaker.id, finalChatChannel.id) }
                            }
                        }
                    })
                }
                return chatChannel
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    fun get(player: ElysiumPlayer): BukkitChatChannel? {
        val playerId = player.id
        if (playerCache.containsKey(playerId)) {
            return get(playerCache.get(playerId) as Int)
        } else {
            try {
                var chatChannel: BukkitChatChannel? = null
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel, chat_channel_speaker WHERE chat_channel_speaker.player_id = ? AND chat_channel_speaker.chat_channel_id = bukkit_chat_channel.id").use({ statement ->
                        statement.setInt(1, player.id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            chatChannel = BukkitChatChannel(
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
                            if (chatChannel != null) {
                                val finalChatChannel = chatChannel!!
                                connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                                    listenerStatement.setInt(1, finalChatChannel.id)
                                    val listenerResultSet = listenerStatement.executeQuery()
                                    while (listenerResultSet.next()) {
                                        finalChatChannel.addListener(plugin.core.database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                                    }
                                })
                                connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                                    speakerStatement.setInt(1, finalChatChannel.id)
                                    val speakerResultSet = speakerStatement.executeQuery()
                                    while (speakerResultSet.next()) {
                                        finalChatChannel.addSpeaker(plugin.core.database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                                    }
                                })
                                cache.put(finalChatChannel.id, chatChannel)
                                nameCache.put(finalChatChannel.name, finalChatChannel.id)
                                finalChatChannel.speakers.forEach { speaker -> playerCache.put(speaker.id, finalChatChannel.id) }
                            }
                        }
                    })
                }
                return chatChannel
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    fun getAll(): Collection<BukkitChatChannel> {
        val chatChannels = ArrayList<BukkitChatChannel>()
        try {
            plugin.core.database.createConnection().use { connection ->
                connection.prepareStatement("SELECT id FROM bukkit_chat_channel").use({ statement ->
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val chatChannel = get(resultSet.getInt("id"))
                        if (chatChannel != null) {
                            connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                                listenerStatement.setInt(1, chatChannel.id)
                                val listenerResultSet = listenerStatement.executeQuery()
                                while (listenerResultSet.next()) {
                                    chatChannel.addListener(plugin.core.database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                                }
                            })
                            connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                                speakerStatement.setInt(1, chatChannel.id)
                                val speakerResultSet = speakerStatement.executeQuery()
                                while (speakerResultSet.next()) {
                                    chatChannel.addSpeaker(plugin.core.database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                                }
                            })
                            cache.put(chatChannel.id, chatChannel)
                            nameCache.put(chatChannel.name, chatChannel.id)
                            chatChannel.speakers.forEach { speaker -> playerCache.put(speaker.id, chatChannel.id) }
                            chatChannels.add(chatChannel)
                        }
                    }
                })
            }
            return chatChannels
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return emptyList()
    }

    override fun delete(`object`: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakersStatement ->
                    speakersStatement.setInt(1, `object`.id)
                    speakersStatement.executeUpdate()
                    cache.remove(`object`.id)
                    nameCache.remove(`object`.name)
                    `object`.speakers.forEach { player -> playerCache.remove(player.id) }
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenersStatement ->
                    listenersStatement.setInt(1, `object`.id)
                    listenersStatement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_chat_channel WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()

                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }
}
