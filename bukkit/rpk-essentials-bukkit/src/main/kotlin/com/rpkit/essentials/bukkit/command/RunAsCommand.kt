package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RunAsCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.runas")) {
            if (args.size >= 2) {
                if (plugin.server.getPlayer(args[0]) != null) {
                    val player = plugin.server.getPlayer(args[0])
                    val commandToRun = StringBuilder()
                    for (i in 1..args.size - 1) {
                        commandToRun.append(args[i]).append(" ")
                    }
                    plugin.server.dispatchCommand(player, commandToRun.toString())
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.run-as-valid")))
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.run-as-invalid-player")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.run-as-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-run-as")))
        }
        return true
    }

}
