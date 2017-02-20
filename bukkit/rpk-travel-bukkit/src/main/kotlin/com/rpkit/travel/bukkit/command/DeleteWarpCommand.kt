package com.rpkit.travel.bukkit.command

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeleteWarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.travel.command.deletewarp")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
                    val warp = warpProvider.getWarp(args[0].toLowerCase())
                    if (warp != null) {
                        warpProvider.removeWarp(warp)
                        sender.sendMessage(plugin.core.messages["delete-warp-valid", mapOf(
                                Pair("warp", warp.name)
                        )])
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["delete-warp-usage"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-delete-warp"])
        }
        return true
    }
}
