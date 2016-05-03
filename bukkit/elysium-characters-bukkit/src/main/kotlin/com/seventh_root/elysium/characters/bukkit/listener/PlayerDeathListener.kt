package com.seventh_root.elysium.characters.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val plugin: ElysiumCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
        val player = playerProvider.getPlayer(event.entity)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null) {
            if (plugin.config.getBoolean("characters.kill-character-on-death")) {
                    character.isDead = true
                    characterProvider.updateCharacter(character)
            }
        }
    }

}
