package com.rpkit.travel.bukkit.command

import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpUseEvent
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
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
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                        if (minecraftProfile == null) {
                            sender.sendMessage(plugin.messages["no-minecraft-profile"])
                            return true
                        }
                        val event = RPKBukkitWarpUseEvent(warp, minecraftProfile)
                        plugin.server.pluginManager.callEvent(event)
                        if (event.isCancelled) return true
                        sender.teleport(event.warp.location)
                        sender.sendMessage(plugin.messages["warp-valid", mapOf(
                                Pair("warp", warp.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.messages["warp-invalid-warp"])
                    }
                } else {
                    if (warpProvider.warps.isNotEmpty()) {
                        sender.sendMessage(plugin.messages["warp-list-title"])
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
                            sender.sendMessage(plugin.messages["warp-list-item", mapOf(
                                    Pair("warps", message)
                            )])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["warp-list-invalid-empty"])
                    }
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-warp"])
        }
        return true
    }

}
