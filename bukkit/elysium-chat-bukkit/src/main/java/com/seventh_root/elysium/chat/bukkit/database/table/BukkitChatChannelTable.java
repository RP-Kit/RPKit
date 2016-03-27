package com.seventh_root.elysium.chat.bukkit.database.table;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel;
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.database.Table;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class BukkitChatChannelTable extends Table<BukkitChatChannel> {

    private ElysiumChatBukkit plugin;

    public BukkitChatChannelTable(ElysiumChatBukkit plugin, Database database) throws SQLException {
        super(database, BukkitChatChannel.class);
        this.plugin = plugin;
    }

    @Override
    public void create() {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
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
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS chat_channel_listener(" +
                                "chat_channel_id INTEGER," +
                                "player_id INTEGER," +
                                "FOREIGN KEY(chat_channel_id) REFERENCES bukkit_chat_channel(id)," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)" +
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS chat_channel_speaker(" +
                                "chat_channel_id INTEGER," +
                                "player_id INTEGER," +
                                "FOREIGN KEY(chat_channel_id) REFERENCES bukkit_chat_channel(id)," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)" +
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public int insert(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bukkit_chat_channel(name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        RETURN_GENERATED_KEYS
                )
        ) {
            statement.setString(1, chatChannel.getName());
            statement.setInt(2, chatChannel.getColor().getRed());
            statement.setInt(3, chatChannel.getColor().getGreen());
            statement.setInt(4, chatChannel.getColor().getBlue());
            statement.setString(5, chatChannel.getFormatString());
            statement.setInt(6, chatChannel.getRadius());
            statement.setInt(7, chatChannel.getClearRadius());
            statement.setString(8, chatChannel.getMatchPattern());
            statement.setBoolean(9, chatChannel.isIRCEnabled());
            statement.setString(10, chatChannel.getIRCChannel());
            statement.setBoolean(11, chatChannel.isIRCWhitelist());
            statement.setBoolean(12, chatChannel.isJoinedByDefault());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                chatChannel.setId(id);
                insertListeners(chatChannel);
                insertSpeakers(chatChannel);
                return id;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private void insertListeners(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO chat_channel_listener(chat_channel_id, player_id) VALUES(?, ?)"
                )
        ) {
            for (ElysiumPlayer listener : chatChannel.getListeners()) {
                statement.setInt(1, chatChannel.getId());
                statement.setInt(2, listener.getId());
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void insertSpeakers(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO chat_channel_speaker(chat_channel_id, player_id) VALUES(?, ?)"
                )
        ) {
            for (ElysiumPlayer speaker : chatChannel.getSpeakers()) {
                statement.setInt(1, chatChannel.getId());
                statement.setInt(2, speaker.getId());
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void update(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bukkit_chat_channel SET name = ?, color_red = ?, color_green = ?, color_blue = ?, format_string = ?, radius = ?, clear_radius = ?, match_pattern = ?, irc_enabled = ?, irc_channel = ?, irc_whitelist = ?, joined_by_default = ? WHERE id = ?"
                )
        ) {
            statement.setString(1, chatChannel.getName());
            statement.setInt(2, chatChannel.getColor().getRed());
            statement.setInt(3, chatChannel.getColor().getGreen());
            statement.setInt(4, chatChannel.getColor().getBlue());
            statement.setString(5, chatChannel.getFormatString());
            statement.setInt(6, chatChannel.getRadius());
            statement.setInt(7, chatChannel.getClearRadius());
            statement.setString(8, chatChannel.getMatchPattern());
            statement.setBoolean(9, chatChannel.isIRCEnabled());
            statement.setString(10, chatChannel.getIRCChannel());
            statement.setBoolean(11, chatChannel.isIRCWhitelist());
            statement.setBoolean(12, chatChannel.isJoinedByDefault());
            statement.setInt(13, chatChannel.getId());
            statement.executeUpdate();
            deleteListeners(chatChannel);
            deleteSpeakers(chatChannel);
            insertListeners(chatChannel);
            insertSpeakers(chatChannel);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void deleteListeners(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM chat_channel_listener WHERE chat_channel_id = ?"
                )
        ) {
            statement.setInt(1, chatChannel.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void deleteSpeakers(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?"
                )
        ) {
            statement.setInt(1, chatChannel.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public BukkitChatChannel get(int id) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel WHERE id = ?"
                )
        ) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                BukkitChatChannel chatChannel = new BukkitChatChannel.Builder(plugin)
                        .id(resultSet.getInt("id"))
                        .name(resultSet.getString("name"))
                        .color(ChatColorUtils.closestChatColorToColor(new Color(resultSet.getInt("color_red"), resultSet.getInt("color_green"), resultSet.getInt("color_blue"))))
                        .formatString(resultSet.getString("format_string"))
                        .radius(resultSet.getInt("radius"))
                        .clearRadius(resultSet.getInt("clear_radius"))
                        .matchPattern(resultSet.getString("match_pattern"))
                        .ircEnabled(resultSet.getBoolean("irc_enabled"))
                        .ircChannel(resultSet.getString("irc_channel"))
                        .ircWhitelist(resultSet.getBoolean("irc_whitelist"))
                        .joinedByDefault(resultSet.getBoolean("joined_by_default"))
                        .build();
                try (
                        PreparedStatement listenerStatement = connection.prepareStatement("SELECT player_id FROM chat_channel_listener WHERE chat_channel_id = ?")
                ) {
                    listenerStatement.setInt(1, chatChannel.getId());
                    ResultSet listenerResultSet = listenerStatement.executeQuery();
                    while (listenerResultSet.next()) {
                        chatChannel.addListener(getDatabase().getTable(BukkitPlayer.class).get(listenerResultSet.getInt("player_id")));
                    }
                }
                try (
                        PreparedStatement speakerStatement = connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?")
                ) {
                    speakerStatement.setInt(1, chatChannel.getId());
                    ResultSet speakerResultSet = speakerStatement.executeQuery();
                    while (speakerResultSet.next()) {
                        chatChannel.addSpeaker(getDatabase().getTable(BukkitPlayer.class).get(speakerResultSet.getInt("player_id")));
                    }
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(BukkitChatChannel chatChannel) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement speakersStatement = connection.prepareStatement(
                "DELETE FROM chat_channel_speaker WHERE chat_channel_id = ?"
        )) {
            speakersStatement.setInt(1, chatChannel.getId());
            speakersStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement listenersStatement = connection.prepareStatement(
                "DELETE FROM chat_channel_listener WHERE chat_channel_id = ?"
        )) {
            listenersStatement.setInt(1, chatChannel.getId());
            listenersStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bukkit_chat_channel WHERE id = ?"
                )
        ) {
            statement.setInt(1, chatChannel.getId());
            statement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
