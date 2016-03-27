package com.seventh_root.elysium.characters.bukkit.command.race;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RaceCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final RaceAddCommand raceAddCommand;
    private final RaceRemoveCommand raceRemoveCommand;
    private final RaceListCommand raceListCommand;

    public RaceCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        this.raceAddCommand = new RaceAddCommand(plugin);
        this.raceRemoveCommand = new RaceRemoveCommand(plugin);
        this.raceListCommand = new RaceListCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new")) {
                return raceAddCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                return raceRemoveCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("list")) {
                return raceListCommand.onCommand(sender, command, label, newArgs);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-usage")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-usage")));
        }
        return true;
    }

}
