package com.seventh_root.elysium.chat.bukkit.chatchannel;

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent;
import com.seventh_root.elysium.api.chat.ChatChannelProvider;
import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitIRCChatChannelPipelineComponent;
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitChatChannelProvider implements ChatChannelProvider<BukkitChatChannel> {

    private final ElysiumChatBukkit plugin;

    public BukkitChatChannelProvider(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public Collection<BukkitChatChannel> getChatChannels() {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel")
        ) {
            ResultSet resultSet = statement.executeQuery();
            List<BukkitChatChannel> chatChannels = new ArrayList<>();
            while (resultSet.next()) {
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
                        chatChannel.addListener(plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(listenerResultSet.getInt("player_id")));
                    }
                }
                try (
                        PreparedStatement speakerStatement = connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?")
                ) {
                    speakerStatement.setInt(1, chatChannel.getId());
                    ResultSet speakerResultSet = speakerStatement.executeQuery();
                    while (speakerResultSet.next()) {
                        chatChannel.addSpeaker(plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(speakerResultSet.getInt("player_id")));
                    }
                }
                chatChannels.add(chatChannel);
            }
            return chatChannels;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public BukkitChatChannel getChatChannel(int id) {
        return plugin.getCore().getDatabase().getTable(BukkitChatChannel.class).get(id);
    }

    @Override
    public BukkitChatChannel getChatChannel(String name) {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel WHERE name = ?")
        ) {
            statement.setString(1, name);
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
                        chatChannel.addListener(plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(listenerResultSet.getInt("player_id")));
                    }
                }
                try (
                        PreparedStatement speakerStatement = connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?")
                ) {
                    speakerStatement.setInt(1, chatChannel.getId());
                    ResultSet speakerResultSet = speakerStatement.executeQuery();
                    while (speakerResultSet.next()) {
                        chatChannel.addSpeaker(plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(speakerResultSet.getInt("player_id")));
                    }
                }
                return chatChannel;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void addChatChannel(BukkitChatChannel chatChannel) {
        plugin.getCore().getDatabase().getTable(BukkitChatChannel.class).insert(chatChannel);
    }

    @Override
    public void removeChatChannel(BukkitChatChannel chatChannel) {
        plugin.getCore().getDatabase().getTable(BukkitChatChannel.class).delete(chatChannel);
    }

    @Override
    public void updateChatChannel(BukkitChatChannel chatChannel) {
        plugin.getCore().getDatabase().getTable(BukkitChatChannel.class).update(chatChannel);
    }

    @Override
    public BukkitChatChannel getPlayerChannel(ElysiumPlayer player) {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT id, name, color_red, color_green, color_blue, format_string, radius, clear_radius, match_pattern, irc_enabled, irc_channel, irc_whitelist, joined_by_default FROM bukkit_chat_channel, chat_channel_speaker WHERE chat_channel_speaker.player_id = ? AND chat_channel_speaker.chat_channel_id = bukkit_chat_channel.id")
        ) {
            statement.setInt(1, player.getId());
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
                        chatChannel.addListener(plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(listenerResultSet.getInt("player_id")));
                    }
                }
                try (
                        PreparedStatement speakerStatement = connection.prepareStatement("SELECT player_id FROM chat_channel_speaker WHERE chat_channel_id = ?")
                ) {
                    speakerStatement.setInt(1, chatChannel.getId());
                    ResultSet speakerResultSet = speakerStatement.executeQuery();
                    while (speakerResultSet.next()) {
                        chatChannel.addSpeaker(plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(speakerResultSet.getInt("player_id")));
                    }
                }
                return chatChannel;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPlayerChannel(ElysiumPlayer player, BukkitChatChannel channel) {
        BukkitChatChannel oldChannel = getPlayerChannel(player);
        if (oldChannel != null) {
            oldChannel.removeSpeaker(player);
            updateChatChannel(oldChannel);
        }
        channel.addSpeaker(player);
        updateChatChannel(channel);
    }

    @Override
    public BukkitChatChannel getChatChannelFromIRCChannel(String ircChannel) {
        for (BukkitChatChannel channel : getChatChannels()) {
            List<? extends ChatChannelPipelineComponent> pipeline = channel.getPipeline();
            if (pipeline != null) {
                for (ChatChannelPipelineComponent component : pipeline) {
                    if (component instanceof BukkitIRCChatChannelPipelineComponent) {
                        BukkitIRCChatChannelPipelineComponent ircComponent = (BukkitIRCChatChannelPipelineComponent) component;
                        if (ircComponent.getIRCChannel().equals(ircChannel)) {
                            return channel;
                        }
                    }
                }
            }
        }
        return null;
    }

}
