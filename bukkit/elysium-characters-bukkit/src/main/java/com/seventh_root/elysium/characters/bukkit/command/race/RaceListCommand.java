package com.seventh_root.elysium.characters.bukkit.command.race;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRace;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RaceListCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;

    public RaceListCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("elysium.characters.command.race.list")) {
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-list-title")));
            for (BukkitRace race : raceProvider.getRaces()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-list-item").replace("$race", race.getName())));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-race-list")));
        }
        return true;
    }

}
