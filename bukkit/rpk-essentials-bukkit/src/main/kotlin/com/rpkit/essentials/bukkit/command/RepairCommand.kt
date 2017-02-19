package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RepairCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.repair")) {
            if (sender is Player) {
                if (sender.inventory.itemInMainHand != null) {
                    sender.inventory.itemInMainHand.durability = 0.toShort()
                    sender.sendMessage(plugin.core.messages["repair-valid"])
                } else {
                    sender.sendMessage(plugin.core.messages["repair-invalid-item"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-repair"])
        }
        return true
    }

}
