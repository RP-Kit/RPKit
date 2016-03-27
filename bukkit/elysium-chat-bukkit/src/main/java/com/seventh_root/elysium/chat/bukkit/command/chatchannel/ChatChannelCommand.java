package com.seventh_root.elysium.chat.bukkit.command.chatchannel;

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChatChannelCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;

    private final ChatChannelJoinCommand chatChannelJoinCommand;
    private final ChatChannelLeaveCommand chatChannelLeaveCommand;
    private final ChatChannelSpeakCommand chatChannelSpeakCommand;
    private final ChatChannelCreateCommand chatChannelCreateCommand;
    private final ChatChannelDeleteCommand chatChannelDeleteCommand;
    private final ChatChannelListCommand chatChannelListCommand;
    private final ChatChannelSetCommand chatChannelSetCommand;

    public ChatChannelCommand(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
        chatChannelJoinCommand = new ChatChannelJoinCommand(plugin);
        chatChannelLeaveCommand = new ChatChannelLeaveCommand(plugin);
        chatChannelSpeakCommand = new ChatChannelSpeakCommand(plugin);
        chatChannelCreateCommand = new ChatChannelCreateCommand(plugin);
        chatChannelDeleteCommand = new ChatChannelDeleteCommand(plugin);
        chatChannelListCommand = new ChatChannelListCommand(plugin);
        chatChannelSetCommand = new ChatChannelSetCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("unmute")) {
                return chatChannelJoinCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("mute")) {
                return chatChannelLeaveCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("speak") || args[0].equalsIgnoreCase("talk")) {
                return chatChannelSpeakCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("add")) {
                return chatChannelCreateCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
                return chatChannelDeleteCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("list")) {
                return chatChannelListCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("modify")) {
                return chatChannelSetCommand.onCommand(sender, command, label, newArgs);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-usage")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-usage")));
        }
        return true;
    }

}
