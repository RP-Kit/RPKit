package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.characters.bukkit.character.field.BukkitCharacterCardFieldProvider
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
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
                var player = playerProvider.getPlayer(sender)
                if (sender.hasPermission("elysiumcharacters.command.character.card.other")) {
                    if (args.size > 0) {
                        val bukkitPlayer = plugin.server.getPlayer(args[0])
                        if (bukkitPlayer != null) {
                            player = playerProvider.getPlayer(bukkitPlayer)
                        }
                    }
                }
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    for (line in plugin.config.getStringList("messages.character-card")) {
                        var filteredLine = ChatColor.translateAlternateColorCodes('&', line)
                        val characterCardFieldProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterCardFieldProvider::class.java)
                        characterCardFieldProvider.characterCardFields.forEach { field -> filteredLine = filteredLine.replace("\$${field.name}", field.get(character)) }
                        sender.sendMessage(filteredLine)
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
