package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpeedCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.speed")) {
            var player: Player? = null
            if (sender is Player) {
                player = sender
            }
            var speed = 0f
            if (args.size >= 2 && plugin.server.getPlayer(args[0]) != null) {
                player = plugin.server.getPlayer(args[0])
                try {
                    speed = java.lang.Float.parseFloat(args[1])
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(plugin.messages["speed-invalid-speed-number"])
                }

            } else if (args.isNotEmpty()) {
                try {
                    speed = java.lang.Float.parseFloat(args[0])
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(plugin.messages["speed-invalid-speed-number"])
                }

            } else {
                if (player != null) {
                    player.flySpeed = 0.1f
                    sender.sendMessage(plugin.messages["speed-reset-valid", mapOf(
                            Pair("player", player.name)
                    )])
                    player.sendMessage(plugin.messages["speed-reset-notification", mapOf(
                            Pair("player", sender.name)
                    )])
                }
                return true
            }
            if (player != null) {
                if (speed >= -1 && speed <= 1) {
                    player.flySpeed = speed
                    sender.sendMessage(plugin.messages["speed-set-valid", mapOf(
                            Pair("player", player.name),
                            Pair("speed", speed.toString())
                    )])
                    player.sendMessage(plugin.messages["speed-set-notification", mapOf(
                            Pair("player", sender.name),
                            Pair("speed", speed.toString())
                    )])
                } else {
                    sender.sendMessage(plugin.messages["speed-invalid-speed-bounds"])
                }
            } else {
                sender.sendMessage(plugin.messages["speed-usage-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-speed"])
        }
        return true
    }

}
