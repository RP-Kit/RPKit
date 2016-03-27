package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CharacterListCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;

    public CharacterListCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("elysium.characters.command.character.list")) {
                Player bukkitPlayer = (Player) sender;
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-list-title")));
                for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-list-item")).replace("$character", character.getName()));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-list")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

}
