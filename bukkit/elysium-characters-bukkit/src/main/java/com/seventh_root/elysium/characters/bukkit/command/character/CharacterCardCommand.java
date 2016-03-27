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

public class CharacterCardCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;

    public CharacterCardCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("elysium.characters.command.character.card.self")) {
                Player bukkitPlayer = (Player) sender;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    for (String line : plugin.getConfig().getStringList("messages.character-card")) {
                        sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', line)
                                        .replace("$name", character.getName())
                                        .replace("$player", character.getPlayer().getName())
                                        .replace("$gender", character.getGender() != null ? character.getGender().getName() : "unset")
                                        .replace("$age", Integer.toString(character.getAge()))
                                        .replace("$race", character.getRace() != null ? character.getRace().getName() : "unset")
                                        .replace("$description", character.getDescription())
                                        .replace("$dead", character.isDead() ? "yes" : "no")
                                        .replace("$health", Double.toString(character.getHealth()))
                                        .replace("$max-health", Double.toString(character.getMaxHealth()))
                                        .replace("$mana", Integer.toString(character.getMana()))
                                        .replace("$max-mana", Integer.toString(character.getMaxMana()))
                                        .replace("$food", Integer.toString(character.getFoodLevel()))
                                        .replace("$max-food", Integer.toString(20))
                                        .replace("$thirst", Integer.toString(character.getThirstLevel()))
                                        .replace("$max-thirst", Integer.toString(20))
                        );
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-card-self")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

}
