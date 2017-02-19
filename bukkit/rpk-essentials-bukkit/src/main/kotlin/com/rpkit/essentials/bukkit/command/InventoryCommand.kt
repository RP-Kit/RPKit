package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class InventoryCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.inventory")) {
            if (args.isNotEmpty()) {
                if (sender is Player) {
                    if (plugin.server.getPlayer(args[0]) != null) {
                        val target = plugin.server.getPlayer(args[0])
                        sender.openInventory(target.inventory)
                        sender.sendMessage(plugin.core.messages["inventory-valid", mapOf(
                                Pair("player", target.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.core.messages["inventory-invalid-player"])
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["not-from-console"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["inventory-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-inventory"])
        }
        return true
    }

}
