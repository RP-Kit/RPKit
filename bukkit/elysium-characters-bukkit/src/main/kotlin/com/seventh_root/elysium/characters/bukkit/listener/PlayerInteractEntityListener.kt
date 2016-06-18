package com.seventh_root.elysium.characters.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.characters.bukkit.character.field.BukkitCharacterCardFieldProvider
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot.HAND

class PlayerInteractEntityListener(private val plugin: ElysiumCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand == HAND) {
            if (event.player.isSneaking || !plugin.config.getBoolean("characters.view-card-requires-sneak")) {
                if (event.rightClicked is Player) {
                    if (event.player.hasPermission("elysium.characters.command.character.card.other")) {
                        val bukkitPlayer = event.rightClicked as Player
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                        val player = playerProvider.getPlayer(bukkitPlayer)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            for (line in plugin.config.getStringList("messages.character-card")) {
                                var filteredLine = ChatColor.translateAlternateColorCodes('&', line)
                                val characterCardFieldProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterCardFieldProvider::class.java)
                                characterCardFieldProvider.characterCardFields.forEach { field -> filteredLine = filteredLine.replace("\$${field.name}", field.get(character)) }
                                event.player.sendMessage(filteredLine)
                            }
                        } else {
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character-other")))
                        }
                    } else {
                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-card-other")))
                    }
                }
            }
        }
    }
}
