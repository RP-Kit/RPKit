package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeleteWarpCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.deletewarp")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
                    val warp = warpProvider.getWarp(args[0].toLowerCase())
                    if (warp != null) {
                        warpProvider.removeWarp(warp)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.delete-warp-valid"))
                                .replace("\$warp", warp.name))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.delete-warp-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-delete-warp")))
        }
        return true
    }
}
