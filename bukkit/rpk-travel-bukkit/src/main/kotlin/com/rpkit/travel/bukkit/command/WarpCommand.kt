package com.rpkit.travel.bukkit.command

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class WarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.travel.command.warp")) {
            if (sender is Player) {
                val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
                if (args.isNotEmpty()) {
                    val warp = warpProvider.getWarp(args[0].toLowerCase())
                    if (warp != null) {
                        sender.teleport(warp.location)
                        sender.sendMessage(plugin.core.messages["warp-valid", mapOf(
                                Pair("warp", warp.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.core.messages["warp-invalid-warp"])
                    }
                } else {
                    if (warpProvider.warps.isNotEmpty()) {
                        sender.sendMessage(plugin.core.messages["warp-list-title"])
                        val warps = warpProvider.warps.map(RPKWarp::name)
                        val warpMessages = ArrayList<String>()
                        var warpsBuilder = StringBuilder()
                        for (i in warps.indices) {
                            warpsBuilder.append(warps[i]).append(", ")
                            if ((i + 1) % 10 == 0) {
                                if (i == warps.size - 1) {
                                    warpsBuilder.delete(warpsBuilder.length - 2, warpsBuilder.length)
                                }
                                warpMessages.add(warpsBuilder.toString())
                                warpsBuilder = StringBuilder()
                            }
                        }
                        if (warpsBuilder.isNotEmpty()) {
                            warpMessages.add(warpsBuilder.delete(warpsBuilder.length - 2, warpsBuilder.length).toString())
                        }
                        for (message in warpMessages) {
                            sender.sendMessage(plugin.core.messages["warp-list-item", mapOf(
                                    Pair("warps", message)
                            )])
                        }
                    } else {
                        sender.sendMessage(plugin.core.messages["warp-list-invalid-empty"])
                    }
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-warp"])
        }
        return true
    }

}
