package com.seventh_root.elysium.chat.bukkit.database.table

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import java.awt.Color
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class BukkitChatChannelTable @Throws(SQLException::class)
constructor(private val plugin: ElysiumChatBukkit, database: Database) : Table<BukkitChatChannel>(database, BukkitChatChannel::class.java) {

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
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

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
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitChatChannel? {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel WHERE id = ?").use({ statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val chatChannel = BukkitChatChannel(
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
                                ircEnabled = resultSet.getBoolean("irc_enabled"),
                                ircChannel = resultSet.getString("irc_channel"),
                                ircWhitelist = resultSet.getBoolean("irc_whitelist"),
                                isJoinedByDefault = resultSet.getBoolean("joined_by_default")
                        )
                        connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                            listenerStatement.setInt(1, chatChannel.id)
                            val listenerResultSet = listenerStatement.executeQuery()
                            while (listenerResultSet.next()) {
                                chatChannel.addListener(database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                            }
                        })
                        connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                            speakerStatement.setInt(1, chatChannel.id)
                            val speakerResultSet = speakerStatement.executeQuery()
                            while (speakerResultSet.next()) {
                                chatChannel.addSpeaker(database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                            }
                        })
                    }
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return null
    }

    override fun delete(`object`: BukkitChatChannel) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakersStatement ->
                    speakersStatement.setInt(1, `object`.id)
                    speakersStatement.executeUpdate()
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
