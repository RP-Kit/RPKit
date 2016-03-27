package com.seventh_root.elysium.characters.bukkit.listener;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private ElysiumCharactersBukkit plugin;

    public PlayerDeathListener(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
        BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
        BukkitPlayer player = playerProvider.getPlayer(event.getEntity());
        BukkitCharacter character = characterProvider.getActiveCharacter(player);
        if (plugin.getConfig().getBoolean("characters.kill-character-on-death")) {
            character.setDead(true);
            characterProvider.updateCharacter(character);
        }
    }

}
