package com.seventh_root.elysium.characters.bukkit.command.race

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RaceListCommand(private val plugin: ElysiumCharactersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("elysium.characters.command.race.list")) {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-title")))
            for (race in raceProvider.races) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-item")
                        .replace("\$race", race.name)))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-race-list")))
        }
        return true
    }

}
