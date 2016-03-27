package com.seventh_root.elysium.chat.bukkit.command.chatchannel;

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChatChannelSetCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;

    private final ChatChannelSetNameCommand chatChannelSetNameCommand;
    private final ChatChannelSetColorCommand chatChannelSetColorCommand;
    private final ChatChannelSetFormatCommand chatChannelSetFormatCommand;
    private final ChatChannelSetRadiusCommand chatChannelSetRadiusCommand;
    private final ChatChannelSetClearRadiusCommand chatChannelSetClearRadiusCommand;
    private final ChatChannelSetMatchPatternCommand chatChannelSetMatchPatternCommand;
    private final ChatChannelSetIRCEnabledCommand chatChannelSetIRCEnabledCommand;
    private final ChatChannelSetIRCChannelCommand chatChannelSetIRCChannelCommand;
    private final ChatChannelSetIRCWhitelistCommand chatChannelSetIRCWhitelistCommand;
    private final ChatChannelSetJoinedByDefaultCommand chatChannelSetJoinedByDefaultCommand;

    public ChatChannelSetCommand(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
        chatChannelSetNameCommand = new ChatChannelSetNameCommand(plugin);
        chatChannelSetColorCommand = new ChatChannelSetColorCommand(plugin);
        chatChannelSetFormatCommand = new ChatChannelSetFormatCommand(plugin);
        chatChannelSetRadiusCommand = new ChatChannelSetRadiusCommand(plugin);
        chatChannelSetClearRadiusCommand = new ChatChannelSetClearRadiusCommand(plugin);
        chatChannelSetMatchPatternCommand = new ChatChannelSetMatchPatternCommand(plugin);
        chatChannelSetIRCEnabledCommand = new ChatChannelSetIRCEnabledCommand(plugin);
        chatChannelSetIRCChannelCommand = new ChatChannelSetIRCChannelCommand(plugin);
        chatChannelSetIRCWhitelistCommand = new ChatChannelSetIRCWhitelistCommand(plugin);
        chatChannelSetJoinedByDefaultCommand = new ChatChannelSetJoinedByDefaultCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("elysium.chat.command.chatchannel.set")) {
            if (args.length > 0) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                if (args[0].equalsIgnoreCase("name")) {
                    chatChannelSetNameCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("color") || args[0].equalsIgnoreCase("colour")) {
                    chatChannelSetColorCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("format")) {
                    chatChannelSetFormatCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("radius")) {
                    chatChannelSetRadiusCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("clearradius")) {
                    chatChannelSetClearRadiusCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("matchpattern")) {
                    chatChannelSetMatchPatternCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("ircenabled")) {
                    chatChannelSetIRCEnabledCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("ircchannel")) {
                    chatChannelSetIRCChannelCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("ircwhitelist")) {
                    chatChannelSetIRCWhitelistCommand.onCommand(sender, command, label, newArgs);
                } else if (args[0].equalsIgnoreCase("joinedbydefault")) {
                    chatChannelSetJoinedByDefaultCommand.onCommand(sender, command, label, newArgs);
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-usage")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-usage")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-set")));
        }
        return true;
    }
}
