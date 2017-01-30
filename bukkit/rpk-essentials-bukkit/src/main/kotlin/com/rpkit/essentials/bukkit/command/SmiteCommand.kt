package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SmiteCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.smite")) {
            if (args.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.smite-usage")))
            } else {
                if (plugin.server.getPlayer(args[0]) == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.smite-invalid-player")))
                } else {
                    val player = plugin.server.getPlayer(args[0])
                    val world = player.world
                    val location = player.location
                    world.strikeLightning(location)
                    player.fireTicks = 100
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.smite-valid"))
                            .replace("\$player", player.name))
                }
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-smite")))
        }
        return true
    }
}