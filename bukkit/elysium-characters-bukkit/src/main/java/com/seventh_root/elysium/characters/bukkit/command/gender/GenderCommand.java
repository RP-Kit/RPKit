package com.seventh_root.elysium.characters.bukkit.command.gender;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GenderCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final GenderAddCommand genderAddCommand;
    private final GenderRemoveCommand genderRemoveCommand;
    private final GenderListCommand genderListCommand;

    public GenderCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        this.genderAddCommand = new GenderAddCommand(plugin);
        this.genderRemoveCommand = new GenderRemoveCommand(plugin);
        this.genderListCommand = new GenderListCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new")) {
                return genderAddCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                return genderRemoveCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("list")) {
                return genderListCommand.onCommand(sender, command, label, newArgs);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-usage")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-usage")));
        }
        return true;
    }

}
