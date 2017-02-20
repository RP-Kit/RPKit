package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FeedCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.feed")) {
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
                player.foodLevel = 20
                player.saturation = 10f
                player.sendMessage(plugin.messages["feed-notification"])
                sender.sendMessage(plugin.messages["feed-valid", mapOf(
                        Pair("player", player.name)
                )])
            } else {
                sender.sendMessage(plugin.messages["feed-usage-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-feed"])
        }
        return true
    }

}
