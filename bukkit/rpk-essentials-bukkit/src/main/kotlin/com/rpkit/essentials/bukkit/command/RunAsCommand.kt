package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RunAsCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.runas")) {
            if (args.size >= 2) {
                val player = plugin.server.getPlayer(args[0])
                if (player != null) {
                    val commandToRun = StringBuilder()
                    for (i in 1 until args.size) {
                        commandToRun.append(args[i]).append(" ")
                    }
                    plugin.server.dispatchCommand(player, commandToRun.toString())
                    sender.sendMessage(plugin.messages["run-as-valid"])
                } else {
                    sender.sendMessage(plugin.messages["run-as-invalid-player"])
                }
            } else {
                sender.sendMessage(plugin.messages["run-as-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-run-as"])
        }
        return true
    }

}
