package com.seventh_root.elysium.characters.bukkit.listener;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {

    private final ElysiumCharactersBukkit plugin;

    public PlayerInteractEntityListener(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().isSneaking() || !plugin.getConfig().getBoolean("characters.view-card-requires-sneak")) {
            if (event.getRightClicked() instanceof Player) {
                if (event.getPlayer().hasPermission("elysium.characters.command.character.card.other")) {
                    Player bukkitPlayer = (Player) event.getRightClicked();
                    BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                    BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                    BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                    BukkitCharacter character = characterProvider.getActiveCharacter(player);
                    if (character != null) {
                        for (String line : plugin.getConfig().getStringList("messages.character-card")) {
                            event.getPlayer().sendMessage(
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
                        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character-other")));
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-card-other")));
                }
            }
        }
    }
}
