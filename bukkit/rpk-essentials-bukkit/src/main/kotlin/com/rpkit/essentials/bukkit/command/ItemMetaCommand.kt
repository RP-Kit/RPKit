package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class ItemMetaCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.itemmeta")) {
            if (sender is Player) {
                if (sender.inventory.itemInMainHand != null && sender.inventory.itemInMainHand.type != Material.AIR) {
                    if (args.size >= 2) {
                        val meta = sender.inventory.itemInMainHand.itemMeta
                        if (args[0].equals("setname", ignoreCase = true)) {
                            val name = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                            meta.displayName = name
                            sender.sendMessage(plugin.core.messages["item-meta-set-name-valid", mapOf(
                                    Pair("name", name)
                            )])
                        } else if (args[0].equals("addlore", ignoreCase = true)) {
                            val lore: MutableList<String>
                            if (meta.hasLore()) {
                                lore = meta.lore
                            } else {
                                lore = ArrayList<String>()
                            }
                            val loreItem = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                            lore.add(loreItem)
                            sender.sendMessage(plugin.core.messages["item-meta-add-lore-valid", mapOf(
                                    Pair("lore", loreItem)
                            )])
                            meta.lore = lore
                        } else if (args[0].equals("removelore", ignoreCase = true)) {
                            if (meta.hasLore()) {
                                val lore = meta.lore
                                val loreItem = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                                if (lore.contains(loreItem)) {
                                    lore.remove(loreItem)
                                    sender.sendMessage(plugin.core.messages["item-meta-remove-lore-valid", mapOf(
                                            Pair("lore", loreItem)
                                    )])
                                } else {
                                    sender.sendMessage(plugin.core.messages["item-meta-remove-lore-invalid-lore-item"])
                                }
                                meta.lore = lore
                            } else {
                                sender.sendMessage(plugin.core.messages["item-meta-remove-lore-invalid-lore"])
                            }
                        } else {
                            sender.sendMessage(plugin.core.messages["item-meta-usage"])
                        }
                        sender.inventory.itemInMainHand.itemMeta = meta
                    } else {
                        sender.sendMessage(plugin.core.messages["item-meta-usage"])
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["item-meta-invalid-item"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-item-meta"])
        }
        return true
    }

}
