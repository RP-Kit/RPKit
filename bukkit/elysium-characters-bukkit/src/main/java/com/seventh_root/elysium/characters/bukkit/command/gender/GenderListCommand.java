package com.seventh_root.elysium.characters.bukkit.command.gender;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GenderListCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;

    public GenderListCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("elysium.characters.command.gender.list")) {
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-list-title")));
            for (BukkitGender gender : genderProvider.getGenders()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-list-item")).replace("$gender", gender.getName()));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-gender-list")));
        }
        return true;
    }

}
