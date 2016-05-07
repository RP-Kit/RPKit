package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CharacterCardCommand(private val plugin: ElysiumCharactersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.characters.command.character.card.self")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    for (line in plugin.config.getStringList("messages.character-card")) {
                        val gender = character.gender
                        val race = character.race
                        sender.sendMessage(
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
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-card-self")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

}
