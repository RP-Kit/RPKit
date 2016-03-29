package com.seventh_root.elysium.characters.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.BLINDNESS

class PlayerMoveListener(private val plugin: ElysiumCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val playerProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
        val characterProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
        val player = playerProvider.getPlayer(event.player)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null && character.isDead) {
            if (event.from.blockX != event.to.blockX || event.from.blockZ != event.to.blockZ) {
                event.player.teleport(Location(event.from.world, event.from.blockX + 0.5, event.from.blockY + 0.5, event.from.blockZ.toDouble(), event.from.yaw, event.from.pitch))
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.dead-character")))
                event.player.addPotionEffect(PotionEffect(BLINDNESS, 60, 1))
            }
        }
    }

}
