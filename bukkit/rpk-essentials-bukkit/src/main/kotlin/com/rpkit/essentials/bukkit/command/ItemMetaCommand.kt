package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ItemMetaCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.itemmeta")) {
            if (sender is Player) {
                val itemInMainHand = sender.inventory.itemInMainHand
                if (itemInMainHand.type != Material.AIR) {
                    if (args.size >= 2) {
                        val meta = itemInMainHand.itemMeta ?: plugin.server.itemFactory.getItemMeta(itemInMainHand.type)
                        if (meta == null) {
                            sender.sendMessage(plugin.messages["item=meta-failed-to-create"])
                            return true
                        }
                        if (args[0].equals("setname", ignoreCase = true)) {
                            val name = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                            meta.setDisplayName(name)
                            sender.sendMessage(plugin.messages["item-meta-set-name-valid", mapOf(
                                    Pair("name", name)
                            )])
                        } else if (args[0].equals("addlore", ignoreCase = true)) {
                            val lore = meta.lore ?: mutableListOf<String>()
                            val loreItem = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                            lore.add(loreItem)
                            sender.sendMessage(plugin.messages["item-meta-add-lore-valid", mapOf(
                                    Pair("lore", loreItem)
                            )])
                            meta.lore = lore
                        } else if (args[0].equals("removelore", ignoreCase = true)) {
                            if (meta.hasLore()) {
                                val lore = meta.lore ?: mutableListOf<String>()
                                val loreItem = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                                if (lore.contains(loreItem)) {
                                    lore.remove(loreItem)
                                    sender.sendMessage(plugin.messages["item-meta-remove-lore-valid", mapOf(
                                            Pair("lore", loreItem)
                                    )])
                                } else {
                                    sender.sendMessage(plugin.messages["item-meta-remove-lore-invalid-lore-item"])
                                }
                                meta.lore = lore
                            } else {
                                sender.sendMessage(plugin.messages["item-meta-remove-lore-invalid-lore"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["item-meta-usage"])
                        }
                        itemInMainHand.itemMeta = meta
                    } else {
                        sender.sendMessage(plugin.messages["item-meta-usage"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["item-meta-invalid-item"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-item-meta"])
        }
        return true
    }

}
