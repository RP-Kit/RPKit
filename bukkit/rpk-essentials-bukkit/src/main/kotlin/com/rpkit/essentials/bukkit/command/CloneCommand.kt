package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CloneCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.clone")) {
            if (sender is Player) {
                if (sender.inventory.itemInMainHand != null) {
                    sender.inventory.addItem(ItemStack(sender.inventory.itemInMainHand))
                    sender.updateInventory()
                    sender.sendMessage(plugin.messages["clone-valid"])
                } else {
                    sender.sendMessage(plugin.messages["clone-invalid-item"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-clone"])
        }
        return true
    }
}
