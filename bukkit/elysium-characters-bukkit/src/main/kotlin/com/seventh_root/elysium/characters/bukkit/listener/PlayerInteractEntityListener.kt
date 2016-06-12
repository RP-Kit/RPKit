package com.seventh_root.elysium.characters.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
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
                                val gender = character.gender
                                val race = character.race
                                event.player.sendMessage(
                                        ChatColor.translateAlternateColorCodes('&', line)
                                                .replace("\$name", character.name)
                                                .replace("\$player", player.name)
                                                .replace("\$gender", if (gender != null) gender.name else "unset")
                                                .replace("\$age", Integer.toString(character.age))
                                                .replace("\$race", if (race != null) race.name else "unset")
                                                .replace("\$description", character.description)
                                                .replace("\$dead", if (character.isDead) "yes" else "no")
                                                .replace("\$health", java.lang.Double.toString(character.health))
                                                .replace("\$max-health", java.lang.Double.toString(character.maxHealth))
                                                .replace("\$mana", Integer.toString(character.mana))
                                                .replace("\$max-mana", Integer.toString(character.maxMana))
                                                .replace("\$food", Integer.toString(character.foodLevel))
                                                .replace("\$max-food", Integer.toString(20))
                                                .replace("\$thirst", Integer.toString(character.thirstLevel))
                                                .replace("\$max-thirst", Integer.toString(20)))
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
