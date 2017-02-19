package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SudoCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.sudo")) {
            val previousOpState = sender.isOp
            if (sender is Player) {
                sender.setOp(true)
            }
            val sudoCommand = StringBuilder()
            for (arg in args) {
                sudoCommand.append(arg).append(" ")
            }
            plugin.server.dispatchCommand(sender, sudoCommand.toString())
            if (sender is Player) {
                sender.setOp(previousOpState)
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-sudo", mapOf(
                    Pair("player", sender.name)
            )])
        }
        return true
    }

}
