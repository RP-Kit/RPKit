package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FlyCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.fly")) {
            var player: Player? = null
            if (sender is Player) {
                player = sender
            }
            if (args.isNotEmpty()) {
                if (plugin.server.getPlayer(args[0]) != null) {
                    player = plugin.server.getPlayer(args[0])
                }
            }
            if (player != null) {
                player.allowFlight = !player.allowFlight
                if (player.allowFlight) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.fly-enable-notification")))
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.fly-enable-valid"))
                            .replace("\$player", player.name))
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.fly-disable-notification")))
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.fly-disable-valid"))
                            .replace("\$player", player.name))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.fly-usage-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-fly")))
        }
        return true
    }

}
