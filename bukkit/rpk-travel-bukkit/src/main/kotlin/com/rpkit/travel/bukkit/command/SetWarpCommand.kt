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
        if (!sender.hasPermission("rpkit.travel.command.setwarp")) {
            sender.sendMessage(plugin.messages["no-permission-set-warp"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!args.isNotEmpty()) {
            sender.sendMessage(plugin.messages["set-warp-usage"])
            return true
        }
        val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
        if (warpProvider.getWarp(args[0]) != null) {
            sender.sendMessage(plugin.messages["set-warp-invalid-name-already-in-use"])
            return true
        }
        val warp = RPKWarpImpl(name = args[0], location = sender.location)
        warpProvider.addWarp(warp)
        sender.sendMessage(plugin.messages["set-warp-valid", mapOf(
                Pair("warp", warp.name),
                Pair("world", warp.location.world?.name ?: ""),
                Pair("x", warp.location.blockX.toString()),
                Pair("y", warp.location.blockY.toString()),
                Pair("z", warp.location.blockZ.toString())
        )])
        return true
    }

}
