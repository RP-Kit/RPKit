package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.item")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val material = Material.matchMaterial(args[0])
                    var amount = 1
                    if (args.size >= 2) {
                        try {
                            amount = Integer.parseInt(args[1])
                        } catch (exception: NumberFormatException) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.item-invalid-amount")))
                            return true
                        }
                    }
                    if (material != null) {
                        val item = ItemStack(material, amount)
                        sender.inventory.addItem(item)
                        if (amount > 1) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.item-valid-plural"))
                                    .replace("\$type", material.toString())
                                    .replace("\$amount", amount.toString()))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.item-valid-singular"))
                                    .replace("\$type", material.toString())
                                    .replace("\$amount", amount.toString()))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.item-invalid-material")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.item-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-item")))
        }
        return true
    }

}
