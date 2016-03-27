package com.seventh_root.elysium.chat.bukkit.listener;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider;
import com.seventh_root.elysium.chat.bukkit.context.BukkitChatMessageContext;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncPlayerChatListener implements Listener {

    private final ElysiumChatBukkit plugin;

    public AsyncPlayerChatListener(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player bukkitPlayer = event.getPlayer();
        BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
        BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
        BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
        BukkitChatChannel channel = chatChannelProvider.getPlayerChannel(player);
        String message = event.getMessage();
        for (BukkitChatChannel otherChannel : chatChannelProvider.getChatChannels()) {
            if (!otherChannel.getMatchPattern().isEmpty() && message.matches(otherChannel.getMatchPattern())) {
                channel = otherChannel;
                Pattern pattern = Pattern.compile(otherChannel.getMatchPattern());
                Matcher matcher = pattern.matcher(message);
                if (matcher.matches()) {
                    if (matcher.groupCount() > 0) {
                        message = matcher.group(1);
                    }
                }
                if (!channel.getListeners().contains(player)) {
                    channel.addListener(player);
                    chatChannelProvider.updateChatChannel(channel);
                }
            }
        }
        if (channel != null) {
            for (ElysiumPlayer listener : channel.getListeners()) {
                if (listener instanceof BukkitPlayer) {
                    BukkitPlayer bukkitListener = (BukkitPlayer) listener;
                    OfflinePlayer bukkitOfflinePlayer = bukkitListener.getBukkitPlayer();
                    if (bukkitOfflinePlayer.isOnline()) {
                        if (channel.getRadius() <= 0
                                || bukkitPlayer.getLocation().distanceSquared(bukkitOfflinePlayer.getPlayer().getLocation()) <= channel.getRadius() * channel.getRadius()) {
                            String processedMessage = channel.processMessage(message, new BukkitChatMessageContext(channel, player, listener));
                            if (processedMessage != null) {
                                bukkitOfflinePlayer.getPlayer().sendMessage(processedMessage);
                            }
                        }
                    }
                }
            }
        } else {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-chat-channel")));
        }
    }

}
