package com.rpkit.travel.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class WarpCommand(private val plugin: com.rpkit.essentials.bukkit.RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.warp")) {
            if (sender is Player) {
                val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
                if (args.isNotEmpty()) {
                    val warp = warpProvider.getWarp(args[0].toLowerCase())
                    if (warp != null) {
                        sender.teleport(warp.location)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.warp-valid"))
                                .replace("\$warp", warp.name))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.warp-invalid-warp")))
                    }
                } else {
                    if (warpProvider.warps.isNotEmpty()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.warp-list-title")))
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
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.warp-list-item"))
                                    .replace("\$warps", message))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.warp-list-invalid-empty")))
                    }
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages-no-permission-warp")))
        }
        return true
    }

}
