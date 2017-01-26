package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class SpawnMobCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.spawnmob")) {
            if (args.size >= 2) {
                if (sender is Player) {
                    try {
                        val entityType = EntityType.valueOf(args[0].toUpperCase())
                        try {
                            val amount = Integer.parseInt(args[1])
                            for (i in 0..amount - 1) {
                                sender.location.world.spawnEntity(sender.location, entityType)
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.spawn-mob-valid")))
                        } catch (exception: NumberFormatException) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.spawn-mob-invalid-amount")))
                        }

                    } catch (exception: IllegalArgumentException) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.spawn-mob-invalid-mob")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.spawn-mob-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-spawn-mob")))
        }
        return true
    }

}
