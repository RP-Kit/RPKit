package com.seventh_root.elysium.characters.bukkit.listener;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;

import static org.bukkit.potion.PotionEffectType.BLINDNESS;

public class PlayerMoveListener implements Listener {

    private final ElysiumCharactersBukkit plugin;

    public PlayerMoveListener(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
        BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
        BukkitPlayer player = playerProvider.getPlayer(event.getPlayer());
        BukkitCharacter character = characterProvider.getActiveCharacter(player);
        if (character != null && character.isDead()) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.getPlayer().teleport(new Location(event.getFrom().getWorld(), event.getFrom().getBlockX() + 0.5, event.getFrom().getBlockY() + 0.5, (double) event.getFrom().getBlockZ(), event.getFrom().getYaw(), event.getFrom().getPitch()));
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.dead-character")));
                event.getPlayer().addPotionEffect(new PotionEffect(BLINDNESS, 60, 1));
            }
        }
    }

}
