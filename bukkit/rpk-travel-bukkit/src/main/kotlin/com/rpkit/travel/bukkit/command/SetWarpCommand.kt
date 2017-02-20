package com.rpkit.travel.bukkit.command

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.warp.RPKWarpImpl
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetWarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.travel.command.setwarp")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
                    val warp = RPKWarpImpl(name = args[0], location = sender.location)
                    warpProvider.addWarp(warp)
                    sender.sendMessage(plugin.core.messages["set-warp-valid", mapOf(
                            Pair("warp", warp.name),
                            Pair("world", warp.location.world.name),
                            Pair("x", warp.location.blockX.toString()),
                            Pair("y", warp.location.blockY.toString()),
                            Pair("z", warp.location.blockZ.toString())
                    )])
                } else {
                    sender.sendMessage(plugin.core.messages["set-warp-usage"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-set-warp"])
        }
        return true
    }

}
