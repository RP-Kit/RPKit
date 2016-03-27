package com.seventh_root.elysium.chat.bukkit.command.chatchannel;

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider;
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChatChannelListCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;

    public ChatChannelListCommand(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("elysium.chat.command.chatchannel.list")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-list-title")));
            for (BukkitChatChannel channel : plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class).getChatChannels()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-list-item"))
                        .replace("$color", ChatColorUtils.closestChatColorToColor(channel.getColor()).toString())
                        .replace("$name", channel.getName()));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-list-channel")));
        }
        return true;
    }

}
