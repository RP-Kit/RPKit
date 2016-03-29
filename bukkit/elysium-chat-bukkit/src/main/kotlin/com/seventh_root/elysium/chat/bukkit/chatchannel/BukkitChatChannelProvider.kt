package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.api.chat.ChatChannelProvider
import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitIRCChatChannelPipelineComponent
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import java.awt.Color
import java.sql.SQLException
import java.util.*

class BukkitChatChannelProvider(private val plugin: ElysiumChatBukkit) : ChatChannelProvider<BukkitChatChannel> {

    override val chatChannels: Collection<BukkitChatChannel>
        get() {
            val chatChannels = ArrayList<BukkitChatChannel>()
            try {
                plugin.core!!.database.createConnection().use { connection ->
                    connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel").use({ statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            val chatChannel = BukkitChatChannel.Builder(plugin)
                                    .id(resultSet.getInt("id"))
                                    .name(resultSet.getString("name"))
                                    .color(
                                        Color(
                                                resultSet.getInt("color_red"),
                                                resultSet.getInt("color_green"),
                                                resultSet.getInt("color_blue")
                                        )
                                    )
                                    .formatString(resultSet.getString("format_string"))
                                    .radius(resultSet.getInt("radius"))
                                    .clearRadius(resultSet.getInt("clear_radius"))
                                    .matchPattern(resultSet.getString("match_pattern"))
                                    .ircEnabled(resultSet.getBoolean("irc_enabled"))
                                    .ircChannel(resultSet.getString("irc_channel"))
                                    .ircWhitelist(resultSet.getBoolean("irc_whitelist"))
                                    .joinedByDefault(resultSet.getBoolean("joined_by_default"))
                                    .build()
                            connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                                listenerStatement.setInt(1, chatChannel.id)
                                val listenerResultSet = listenerStatement.executeQuery()
                                while (listenerResultSet.next()) {
                                    chatChannel.addListener(plugin.core!!.database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                                }
                            })
                            connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                                speakerStatement.setInt(1, chatChannel.id)
                                val speakerResultSet = speakerStatement.executeQuery()
                                while (speakerResultSet.next()) {
                                    chatChannel.addSpeaker(plugin.core!!.database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                                }
                            })
                            chatChannels.add(chatChannel)
                        }
                    })
                }
                return chatChannels
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }

            return emptyList()
        }

    override fun getChatChannel(id: Int): BukkitChatChannel? {
        return plugin.core!!.database.getTable(BukkitChatChannel::class.java)!![id]
    }

    override fun getChatChannel(name: String): BukkitChatChannel? {
        try {
            var chatChannel: BukkitChatChannel? = null
            plugin.core!!.database.createConnection().use { connection ->
                connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel WHERE name = ?").use({ statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        chatChannel = BukkitChatChannel.Builder(plugin)
                                .id(resultSet.getInt("id"))
                                .name(resultSet.getString("name"))
                                .color(
                                    Color(
                                            resultSet.getInt("color_red"),
                                            resultSet.getInt("color_green"),
                                            resultSet.getInt("color_blue")
                                    )
                                )
                                .formatString(resultSet.getString("format_string"))
                                .radius(resultSet.getInt("radius"))
                                .clearRadius(resultSet.getInt("clear_radius"))
                                .matchPattern(resultSet.getString("match_pattern"))
                                .ircEnabled(resultSet.getBoolean("irc_enabled"))
                                .ircChannel(resultSet.getString("irc_channel"))
                                .ircWhitelist(resultSet.getBoolean("irc_whitelist"))
                                .joinedByDefault(resultSet.getBoolean("joined_by_default"))
                                .build()
                        connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                            listenerStatement.setInt(1, chatChannel!!.id)
                            val listenerResultSet = listenerStatement.executeQuery()
                            while (listenerResultSet.next()) {
                                chatChannel!!.addListener(plugin.core!!.database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                            }
                        })
                        connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                            speakerStatement.setInt(1, chatChannel!!.id)
                            val speakerResultSet = speakerStatement.executeQuery()
                            while (speakerResultSet.next()) {
                                chatChannel!!.addSpeaker(plugin.core!!.database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                            }
                        })
                    }
                })
            }
            return chatChannel
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    override fun addChatChannel(chatChannel: BukkitChatChannel) {
        plugin.core!!.database.getTable(BukkitChatChannel::class.java)!!.insert(chatChannel)
    }

    override fun removeChatChannel(chatChannel: BukkitChatChannel) {
        plugin.core!!.database.getTable(BukkitChatChannel::class.java)!!.delete(chatChannel)
    }

    override fun updateChatChannel(chatChannel: BukkitChatChannel) {
        plugin.core!!.database.getTable(BukkitChatChannel::class.java)!!.update(chatChannel)
    }

    override fun getPlayerChannel(player: ElysiumPlayer): BukkitChatChannel? {
        try {
            var chatChannel: BukkitChatChannel? = null
            plugin.core!!.database.createConnection().use { connection ->
                connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel, chat_channel_speaker WHERE chat_channel_speaker.player_id = ? AND chat_channel_speaker.chat_channel_id = bukkit_chat_channel.id").use({ statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        chatChannel = BukkitChatChannel.Builder(plugin)
                                .id(resultSet.getInt("id"))
                                .name(resultSet.getString("name"))
                                .color(
                                    Color(
                                            resultSet.getInt("color_red"),
                                            resultSet.getInt("color_green"),
                                            resultSet.getInt("color_blue")
                                    )
                                )
                                .formatString(resultSet.getString("format_string"))
                                .radius(resultSet.getInt("radius"))
                                .clearRadius(resultSet.getInt("clear_radius"))
                                .matchPattern(resultSet.getString("match_pattern"))
                                .ircEnabled(resultSet.getBoolean("irc_enabled"))
                                .ircChannel(resultSet.getString("irc_channel"))
                                .ircWhitelist(resultSet.getBoolean("irc_whitelist"))
                                .joinedByDefault(resultSet.getBoolean("joined_by_default"))
                                .build()
                        connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?").use({ listenerStatement ->
                            listenerStatement.setInt(1, chatChannel!!.id)
                            val listenerResultSet = listenerStatement.executeQuery()
                            while (listenerResultSet.next()) {
                                chatChannel!!.addListener(plugin.core!!.database.getTable(BukkitPlayer::class.java)!![listenerResultSet.getInt("player_id")]!!)
                            }
                        })
                        connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?").use({ speakerStatement ->
                            speakerStatement.setInt(1, chatChannel!!.id)
                            val speakerResultSet = speakerStatement.executeQuery()
                            while (speakerResultSet.next()) {
                                chatChannel!!.addSpeaker(plugin.core!!.database.getTable(BukkitPlayer::class.java)!![speakerResultSet.getInt("player_id")]!!)
                            }
                        })
                    }
                })
            }
            return chatChannel
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    override fun setPlayerChannel(player: ElysiumPlayer, channel: BukkitChatChannel) {
        val oldChannel = getPlayerChannel(player)
        if (oldChannel != null) {
            oldChannel.removeSpeaker(player)
            updateChatChannel(oldChannel)
        }
        channel.addSpeaker(player)
        updateChatChannel(channel)
    }

    override fun getChatChannelFromIRCChannel(ircChannel: String): BukkitChatChannel? {
        for (channel in chatChannels) {
            val pipeline = channel.pipeline
            for (component in pipeline) {
                if (component is BukkitIRCChatChannelPipelineComponent) {
                    if (component.ircChannel == ircChannel) {
                        return channel
                    }
                }
            }
        }
        return null
    }

}
