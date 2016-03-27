package com.seventh_root.elysium.chat.bukkit.listener;

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final ElysiumChatBukkit plugin;

    public PlayerJoinListener(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
            BukkitPlayer player = playerProvider.getPlayer(event.getPlayer());
            BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
            chatChannelProvider.getChatChannels().stream().filter(BukkitChatChannel::isJoinedByDefault).forEach(chatChannel -> {
                chatChannel.addListener(player);
                chatChannelProvider.updateChatChannel(chatChannel);
            });
        }
    }

}
